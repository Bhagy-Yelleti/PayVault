package com.payvault.user.dto;

// devOtp is only populated because this is a college project with no real SMS gateway.
// In production this field would never be returned to the client.
public record OtpGenerateResponse(
        String message,
        String devOtp
) {}
