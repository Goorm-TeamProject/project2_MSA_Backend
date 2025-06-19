package com.eouil.msa.users.services;


import com.eouil.msa.shared.jwt.JwtUtil;
import com.eouil.msa.shared.kafka.UserCreatedEvent;
import com.eouil.msa.shared.redis.RedisTokenService;
import com.eouil.msa.users.domains.User;
import com.eouil.msa.users.dtos.JoinRequest;
import com.eouil.msa.users.dtos.JoinResponse;
import com.eouil.msa.users.dtos.LoginRequest;
import com.eouil.msa.users.dtos.LoginResponse;
import com.eouil.msa.users.exceptions.*;
import com.eouil.msa.users.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTokenService redisTokenService;
    private final JwtUtil jwtUtil;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;


    @Value("${aws.sqs.userCreatedQueueUrl}")
    private String userCreatedQueueUrl;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       Environment env,
                       RedisTemplate<String, String> redisTemplate,
                       RedisTokenService redisTokenService,
                       JwtUtil jwtUtil,
                       SqsClient sqsClient,
                       ObjectMapper objectMapper)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        this.redisTemplate = redisTemplate;
        this.redisTokenService = redisTokenService;
        this.jwtUtil = jwtUtil;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }


    public boolean isLocal() {
        return Arrays.asList(env.getActiveProfiles()).contains("local");
    }

    public JoinResponse join(JoinRequest joinRequest) {
        log.info("➡️ [JOIN] 요청 - email: {}", joinRequest.email);

        if (userRepository.findByEmail(joinRequest.email).isPresent()) {
            log.warn("[JOIN] 중복 이메일 시도 - {}", joinRequest.email);
            throw new DuplicateEmailException();
        }

        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setUserId(userId);
        user.setName(joinRequest.name);
        user.setEmail(joinRequest.email);
        user.setPassword(passwordEncoder.encode(joinRequest.password));
        userRepository.save(user);

        // Kafka 이벤트 발행
        UserCreatedEvent event = new UserCreatedEvent(userId, user.getEmail(), user.getName());

        try {
            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(userCreatedQueueUrl)
                    .messageBody(messageBody)
                    .messageGroupId("user-group")
                    .messageDeduplicationId(UUID.randomUUID().toString())
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(request);
            log.info("[JOIN - SQS] SQS 전송 성공 - MessageId: {}, Event: {}", response.messageId(), event);

        } catch (Exception e) {
            log.error("[JOIN - SQS] SQS 전송 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
        }

        return new JoinResponse(user.getName(), user.getEmail(), user.getUserId());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        log.info("[LOGIN] 요청 - email: {}", loginRequest.email);

        User user = userRepository.findByEmail(loginRequest.email)
                .orElseThrow(() -> new UserNotFoundException(loginRequest.email));

        if (!passwordEncoder.matches(loginRequest.password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        redisTokenService.saveRefreshToken(user.getUserId(), refreshToken, jwtUtil.getRefreshTokenExpireMillis());

        log.info("[LOGIN] 성공 - userId: {}", user.getUserId());
        return new LoginResponse(accessToken, refreshToken);
    }


    public void logout(String token) {
        log.info("[LOGOUT] 요청");

        if (token == null || token.isEmpty()) {
            throw new TokenMissingException();
        }

        String userId = jwtUtil.validateTokenAndGetUserId(token);
        redisTokenService.deleteRefreshToken(userId);

        long expireMillis = Math.max(0, jwtUtil.getExpiration(token) - System.currentTimeMillis());
        redisTokenService.addToBlacklist(token, expireMillis);
        log.info("[LOGOUT] 완료 - userId: {}", userId);
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        log.info("[REFRESH] 요청");

        String userId = jwtUtil.validateTokenAndGetUserId(refreshToken);
        String storedRefreshToken = redisTokenService.getRefreshToken(userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String newAccessToken = jwtUtil.generateAccessToken(userId);

        return new LoginResponse(newAccessToken, refreshToken);
    }


    private void saveSecretToRedis(String email, String secret) {
        redisTemplate.opsForHash().put("MFA:SECRETS", email, secret);
    }

    private String getSecretFromRedis(String email) {
        Object secret = redisTemplate.opsForHash().get("MFA:SECRETS", email);
        if (secret == null) throw new MfaSecretNotFoundException("Redis에서 " + email);
        return (String) secret;
    }


    public String getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}