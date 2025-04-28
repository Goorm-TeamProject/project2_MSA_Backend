package com.eouil.msa.users.exceptions;

public class TokenMissingException extends RuntimeException {
    public TokenMissingException() {
        super("토큰이 없거나 만료되었습니다.");
    }
}
