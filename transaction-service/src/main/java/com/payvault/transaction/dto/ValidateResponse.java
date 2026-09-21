package com.payvault.transaction.dto;

import java.math.BigDecimal;

public record ValidateResponse(boolean valid, String message, BigDecimal senderBalance) {}
