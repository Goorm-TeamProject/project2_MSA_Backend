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
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final Environment env;
    private final RedisTemplate<String, String> redisTemplate;
    private  final RedisTokenService redisTokenService;
    private final JwtUtil jwtUtil;
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       Environment env,
                       RedisTemplate<String, String> redisTemplate,
                       RedisTokenService redisTokenService,
                       JwtUtil jwtUtil,
                       KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        this.redisTemplate = redisTemplate;
        this.redisTokenService = redisTokenService;
        this.jwtUtil = jwtUtil;
        this.kafkaTemplate = kafkaTemplate;
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
        kafkaTemplate.send("user.created", event);
        log.info("[JOIN] Kafka Event 전송 완료 - {}", event);

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

        boolean mfaRegistered = user.getMfaSecret() != null;
        log.info("[LOGIN] 성공 - userId: {}, MFA 등록 여부: {}", user.getUserId(), mfaRegistered);
        return new LoginResponse(accessToken, refreshToken, mfaRegistered);
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        log.info("[REFRESH] 요청");

        String userId = jwtUtil.validateTokenAndGetUserId(refreshToken);
        String storedRefreshToken = redisTokenService.getRefreshToken(userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        boolean mfaRegistered = user.getMfaSecret() != null;

        return new LoginResponse(newAccessToken, refreshToken, mfaRegistered);
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

    public String generateOtpUrlByToken(String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String secret = gAuth.createCredentials().getKey();
        try {
            saveSecret(user, secret);
        } catch (Exception e) {
            log.warn("Redis 저장 실패 → fallback to H2 저장: {}", e.getMessage());
            saveSecretToH2(user.getEmail(), secret);
        }

        return String.format("otpauth://totp/%s?secret=%s&issuer=EouilBank", user.getEmail(), secret);
    }

    public boolean verifyCode(String email, int code) {
        String secret = isLocal() ? getSecretFromH2(email) : getSecretFromRedis(email);
        return gAuth.authorize(secret, code);
    }

    private void saveSecretToH2(String email, String secret) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        user.setMfaSecret(secret);
        userRepository.save(user);
    }

    private String getSecretFromH2(String email) {
        return userRepository.findByEmail(email)
                .map(User::getMfaSecret)
                .orElseThrow(() -> new MfaSecretNotFoundException("H2에서 " + email));
    }

    private void saveSecretToRedis(String email, String secret) {
        redisTemplate.opsForHash().put("MFA:SECRETS", email, secret);
    }

    private String getSecretFromRedis(String email) {
        Object secret = redisTemplate.opsForHash().get("MFA:SECRETS", email);
        if (secret == null) throw new MfaSecretNotFoundException("Redis에서 " + email);
        return (String) secret;
    }

    private void saveSecret(User user, String secret) {
        if (isLocal()) {
            saveSecretToH2(user.getEmail(), secret);
        } else {
            saveSecretToRedis(user.getEmail(), secret);
        }
    }


}
