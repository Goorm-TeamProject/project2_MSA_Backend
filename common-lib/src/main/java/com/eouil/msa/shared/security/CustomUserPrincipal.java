package com.eouil.msa.shared.security;

public class CustomUserPrincipal {
    private final String userId;

    public CustomUserPrincipal(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
