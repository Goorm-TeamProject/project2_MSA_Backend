package com.eouil.msa.users.exceptions;

public class MfaSecretNotFoundException extends RuntimeException {
    public MfaSecretNotFoundException(String message) {
        super("MFA 시크릿을 찾을 수 없습니다: " + message);
    }
}
