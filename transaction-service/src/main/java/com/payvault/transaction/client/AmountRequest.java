package com.payvault.transaction.client;

import java.math.BigDecimal;

public record AmountRequest(BigDecimal amount, String reference) {}
