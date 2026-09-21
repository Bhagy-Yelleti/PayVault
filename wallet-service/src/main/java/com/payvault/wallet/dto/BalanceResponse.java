package com.payvault.wallet.dto;

import java.math.BigDecimal;

public record BalanceResponse(Long userId, BigDecimal balance, String currency) {}
