package com.payvault.gateway.filter;

import com.payvault.gateway.config.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

// Global filter: runs on every request through the gateway.
// Public endpoints (register/login/otp) pass straight through.
// Everything else must carry a valid "Authorization: Bearer <token>" header.
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> OPEN_PATHS = List.of(
            "/api/users/register",
            "/api/users/login",
            "/api/users/otp/generate",
            "/api/users/otp/verify"
    );

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (OPEN_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing Authorization header");
        }

        String token = authHeaders.get(0).substring(7);
        if (!jwtUtil.isValid(token)) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"error\":\"" + message + "\"}").getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
