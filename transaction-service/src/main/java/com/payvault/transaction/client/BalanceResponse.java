package com.payvault.transaction.client;

import java.math.BigDecimal;

public record BalanceResponse(Long userId, BigDecimal balance, String currency) {}
