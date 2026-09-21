package com.payvault.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Used for both credit and debit — same shape, different endpoint.
public record AmountRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,
        String reference
) {}
