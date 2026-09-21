package com.payvault.transaction.client;

public record UserProfileResponse(Long id, String name, String email, String phone, boolean isVerified) {}
