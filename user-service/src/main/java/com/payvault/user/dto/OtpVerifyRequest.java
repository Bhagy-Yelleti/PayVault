package com.payvault.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpVerifyRequest(
        @NotNull Long userId,
        @NotBlank String otpCode,
        @NotBlank String purpose
) {}
