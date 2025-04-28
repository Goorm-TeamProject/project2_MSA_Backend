package com.eouil.msa.users.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("유효하지 않거나 만료된 리프레시 토큰입니다.");
    }
}
