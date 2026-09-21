package com.payvault.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// IMPORTANT: this exact secret string must be identical in api-gateway, user-service,
// wallet-service and transaction-service — it is how every service verifies a token
// that User Service issued. In production this would live in a secrets manager.
@Component
public class JwtUtil {

    private static final String SECRET = "payvault-shared-jwt-signing-secret-key-2026-change-in-production-please";
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 24 hours

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

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

    public Long extractUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }
}
