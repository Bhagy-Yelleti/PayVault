package com.payvault.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PinVerifyRequest(
        @NotBlank String pin
) {}
