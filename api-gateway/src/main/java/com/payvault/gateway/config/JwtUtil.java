package com.payvault.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

// Same shared secret is used by User Service to SIGN tokens and here to VALIDATE them.
// In a real production system this would come from a secrets manager, not a property file.
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

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
