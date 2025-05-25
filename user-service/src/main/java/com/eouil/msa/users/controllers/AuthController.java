package com.eouil.msa.users.controllers;

import com.eouil.msa.users.dtos.*;
import com.eouil.msa.users.services.AuthService;
import com.eouil.msa.shared.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

import static com.eouil.msa.users.utils.MaskingUtil.maskEmail;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping("/join")
    public ResponseEntity<Void> join(@Valid @RequestBody JoinRequest joinRequest) {
        log.info("[POST /join] 회원가입 요청 - name: {}, email: {}", joinRequest.getName(), maskEmail(joinRequest.getEmail()));

        JoinResponse joinResponse = authService.join(joinRequest);
        log.info("[POST /join] 회원가입 완료 - userId: {}, name: {}", joinResponse.getUserId(), joinResponse.getName());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse loginResponse = authService.login(loginRequest);

        // 쿠키를 세팅하지 않고 Body 로만 리턴
        return ResponseEntity.ok(Map.of(
                "mfaRegistered", loginResponse.isMfaRegistered(),
                "accessToken",  loginResponse.getAccessToken(),
                "refreshToken", loginResponse.getRefreshToken()
        ));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "accessToken", required = false) String token,
            HttpServletResponse response
    ) {
        log.info("[POST /logout] 로그아웃 요청");

        if (token != null && !token.isBlank()) {
            authService.logout(token);
        }

        // accessToken, refreshToken 쿠키 제거
        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("None")
                // .domain(".eouil.com")
                .maxAge(0)
                .build();

        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("None")
                // .domain(".eouil.com")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        log.info("[POST /logout] 로그아웃 완료");
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        log.info("[POST /refresh] 토큰 갱신 요청");

        LoginResponse loginResponse = authService.refreshAccessToken(refreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", loginResponse.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("None")
                // .domain(".eouil.com")
                .maxAge(Duration.ofMinutes(5))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        log.info("[POST /refresh] accessToken 재발급 완료");
        return ResponseEntity.ok(Map.of("mfaRegistered", loginResponse.isMfaRegistered()));
    }

    @GetMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa(
            @RequestHeader("Authorization") String bearer
    ) {
        // 로그인 때 발급받은 Bearer 토큰으로만 접근 허용
        String token = bearer.substring(7);
        String otpUrl = authService.generateOtpUrlByToken(token);
        return ResponseEntity.ok(Map.of("otpUrl", otpUrl));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<?> verifyMfa(
            @RequestHeader("Authorization") String bearer,
            @RequestBody Map<String,String> payload,
            HttpServletResponse response
    ) {
        String token = bearer.substring(7);
        String email = payload.get("email");
        int code = Integer.parseInt(payload.get("code"));

        if (!authService.verifyCode(email, code)) {
            return ResponseEntity.status(401).body(Map.of("success", false));
        }

        // 검증이 끝난 사용자(=MFA 통과)에게만 Cookie 로 JWT 발급
        String userId = authService.getUserIdByEmail(email);
        String newAt = jwtUtil.generateAccessToken(userId, true);
        String newRt = jwtUtil.generateRefreshToken(userId);

        ResponseCookie atCookie = ResponseCookie.from("accessToken", newAt)
                .httpOnly(true).secure(false).sameSite("Lax")
                .path("/").maxAge(Duration.ofMinutes(5)).build();
        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", newRt)
                .httpOnly(true).secure(false).sameSite("Lax")
                .path("/").maxAge(Duration.ofDays(7)).build();
        response.addHeader(HttpHeaders.SET_COOKIE, atCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return ResponseEntity.ok(Map.of("success", true));
    }


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ALB User-service Health Check OK");
    }
}