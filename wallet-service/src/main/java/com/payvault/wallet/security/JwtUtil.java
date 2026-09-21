package com.payvault.wallet.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

// Must match the secret used by user-service to sign tokens.
@Component
public class JwtUtil {

    private static final String SECRET = "payvault-shared-jwt-signing-secret-key-2026-change-in-production-please";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
