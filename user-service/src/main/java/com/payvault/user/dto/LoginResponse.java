package com.payvault.user.dto;

public record LoginResponse(
        String token,
        Long userId,
        String name,
        String email
) {}
