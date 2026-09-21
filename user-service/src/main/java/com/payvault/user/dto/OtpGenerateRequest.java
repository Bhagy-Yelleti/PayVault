package com.payvault.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpGenerateRequest(
        @NotNull Long userId,
        @NotBlank String purpose
) {}
