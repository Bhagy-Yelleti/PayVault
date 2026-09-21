package com.payvault.transaction.dto;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryResponse(Map<String, BigDecimal> categoryTotals, BigDecimal totalSpent) {}
