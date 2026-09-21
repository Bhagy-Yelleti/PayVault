package com.payvault.transaction.client;

import java.math.BigDecimal;

public record WalletOperationResponse(Long userId, BigDecimal newBalance, String status) {}
