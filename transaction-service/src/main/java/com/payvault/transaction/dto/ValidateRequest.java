package com.payvault.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ValidateRequest(
        @NotNull Long senderId,
        @NotNull Long receiverId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {}
