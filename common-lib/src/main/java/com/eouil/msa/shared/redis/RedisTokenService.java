package com.eouil.msa.shared.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenService {
    private final RedisTemplate<String, String> redisTemplate;

    // 저장 (리프레시 토큰)
    public void saveRefreshToken(String username, String refreshToken, long expireTime) {
        // Refresh Token
        redisTemplate.opsForValue().set("RT:" + username, refreshToken, Duration.ofMillis(expireTime));
    }

    // 조회
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get("RT:" + username);
    }

    // 삭제
    public void deleteRefreshToken(String username) {
        redisTemplate.delete("RT:" + username);
    }

    // 블랙리스트 등록 (AccessToken)
    public void addToBlacklist(String accessToken, long expireTime) {
        // BlackList Token
        redisTemplate.opsForValue().set("BL:" + accessToken, "logout", Duration.ofMillis(expireTime));
    }

    // 블랙리스트 조회
    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey("BL:" + accessToken);
    }
}