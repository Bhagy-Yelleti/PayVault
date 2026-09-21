package com.payvault.user.dto;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        boolean isVerified
) {}
