package com.springboot.backendserver.util;

public final class TokenUtils {

    private TokenUtils() {
    }

    public static String extractBearer(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
