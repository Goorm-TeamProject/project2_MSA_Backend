package com.eouil.msa.users.controllers;

import com.eouil.msa.users.dtos.*;
import com.eouil.msa.users.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.eouil.msa.users.utils.MaskingUtil.maskEmail;
import static com.eouil.msa.users.utils.MaskingUtil.maskToken;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    public ResponseEntity<Void> join(@Valid @RequestBody JoinRequest joinRequest) {
        log.info("[POST /join] 회원가입 요청 - name: {}, email: {}", joinRequest.getName(), maskEmail(joinRequest.getEmail()));

        JoinResponse joinResponse = authService.join(joinRequest); //서비스 호출

        log.info("[POST /join] 회원가입 완료 - userId: {}, name: {}", joinResponse.getUserId(), joinResponse.getName());

        return ResponseEntity.ok().build();
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("[POST /login] 로그인 요청 - email: {}", maskEmail(loginRequest.getEmail()));

        LoginResponse loginResponse = authService.login(loginRequest);

        log.info("[POST /login] 로그인 성공 - accessToken: {}", maskToken(loginResponse.getAccessToken()));

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("[POST /refresh] 토큰 갱신 요청");

        String refreshToken = extractToken(authorizationHeader);

        LoginResponse response = authService.refreshAccessToken(refreshToken);

        log.info("[POST /refresh] accessToken 재발급 완료 - MFA 등록 여부: {}", response.isMfaRegistered());

        return ResponseEntity.ok(response);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }



    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String token) {
        log.info("[POST /logout] 로그아웃 요청");
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        authService.logout(token);
        log.info("[POST /logout] 로그아웃 완료");

        return ResponseEntity.ok(new LogoutResponse("로그아웃 완료"));
    }

    @GetMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String otpUrl = authService.generateOtpUrlByToken(token);

        log.info("[GET /mfa/setup] MFA URL 생성 완료");
        return ResponseEntity.ok(Map.of("otpUrl", otpUrl));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<?> verifyMfa(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        int code = Integer.parseInt(payload.get("code"));

        boolean result = authService.verifyCode(email, code);
        return ResponseEntity.ok(Map.of("success", result));
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ALB Health Check OK");
    }
}