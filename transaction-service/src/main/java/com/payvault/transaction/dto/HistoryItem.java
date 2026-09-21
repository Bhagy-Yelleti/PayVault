package com.payvault.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoryItem(
        Long id,
        Long senderId,
        Long receiverId,
        BigDecimal amount,
        String category,
        String status,
        LocalDateTime createdAt
) {}
