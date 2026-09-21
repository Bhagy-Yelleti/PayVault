package com.payvault.user.dto;

public record UpdateProfileRequest(
        String name,
        String phone
) {}
