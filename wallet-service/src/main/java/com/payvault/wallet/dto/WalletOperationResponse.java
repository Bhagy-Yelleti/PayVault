package com.payvault.wallet.dto;

import java.math.BigDecimal;

public record WalletOperationResponse(Long userId, BigDecimal newBalance, String status) {}
