package com.eouil.msa.users.utils;

public class MaskingUtil {
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "masked";
        String[] parts = email.split("@");
        String localPart = parts[0];
        if (localPart.length() <= 1) {
            return "***@" + parts[1];
        }
        return localPart.charAt(0) + "*****@" + parts[1];
    }

    public static String maskToken(String token) {
        if (token == null || token.length() < 10) return "masked-token";
        return token.substring(0, 6) + "*****";
    }

}
