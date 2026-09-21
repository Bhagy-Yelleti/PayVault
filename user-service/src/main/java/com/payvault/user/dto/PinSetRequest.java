package com.payvault.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PinSetRequest(
        @NotBlank @Pattern(regexp = "\\d{4,6}", message = "PIN must be 4-6 digits") String pin
) {}
