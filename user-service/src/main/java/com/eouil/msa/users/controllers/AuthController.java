package com.eouil.msa.users.controllers;

import com.eouil.msa.users.dtos.JoinRequest;
import com.eouil.msa.users.dtos.LoginRequest;
import com.eouil.msa.users.dtos.LoginResponse;
import com.eouil.msa.users.dtos.JoinResponse;
import com.eouil.msa.users.services.AuthService;
import com.eouil.msa.shared.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

import static com.eouil.msa.users.utils.MaskingUtil.maskEmail;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/join")
    public ResponseEntity<Void> join(@Valid @RequestBody JoinRequest joinRequest) {
        log.info("[POST /join] 회원가입 요청 - name: {}, email: {}",
                joinRequest.getName(), maskEmail(joinRequest.getEmail()));

        JoinResponse joinResponse = authService.join(joinRequest);
        log.info("[POST /join] 회원가입 완료 - userId: {}, name: {}",
                joinResponse.getUserId(), joinResponse.getName());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        log.info("[POST /login] 로그인 요청 - email: {}",
                maskEmail(loginRequest.getEmail()));

        LoginResponse loginResponse = authService.login(loginRequest);

        // 1) Access Token 쿠키
        ResponseCookie atCookie = ResponseCookie.from("accessToken", loginResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();

        // 2) Refresh Token 쿠키
        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, atCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "accessToken", required = false) String token,
            HttpServletResponse response
    ) {
        log.info("[POST /logout] 로그아웃 요청");

        if (token != null && !token.isBlank()) {
            authService.logout(token);
        }

        // 쿠키 삭제
        ResponseCookie deleteAt = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie deleteRt = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/api/users/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAt.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRt.toString());

        log.info("[POST /logout] 로그아웃 완료");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        log.info("[POST /refresh] 토큰 갱신 요청");

        LoginResponse loginResponse = authService.refreshAccessToken(refreshToken);

        ResponseCookie atCookie = ResponseCookie.from("accessToken", loginResponse.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, atCookie.toString());

        log.info("[POST /refresh] accessToken 재발급 완료");
        return ResponseEntity.ok().build();
    }



    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ALB User-service Health Check OK");
    }
}
