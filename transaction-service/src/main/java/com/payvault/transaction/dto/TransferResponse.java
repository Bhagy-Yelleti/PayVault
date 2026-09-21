package com.payvault.transaction.dto;

public record TransferResponse(Long transactionId, String status, String message) {}
