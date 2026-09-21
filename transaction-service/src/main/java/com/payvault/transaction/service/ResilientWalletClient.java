package com.payvault.transaction.service;

import com.payvault.transaction.client.AmountRequest;
import com.payvault.transaction.client.BalanceResponse;
import com.payvault.transaction.client.WalletOperationResponse;
import com.payvault.transaction.client.WalletServiceClient;
import com.payvault.transaction.exception.TransactionServiceExceptions.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Wraps every Wallet Service call with a circuit breaker + retry. This has to live in
// its own Spring bean (not called from within TransactionService via "this.") because
// Resilience4j's annotations work through a proxy — self-invocation would skip it.
@Component
public class ResilientWalletClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientWalletClient.class);

    private final WalletServiceClient walletServiceClient;

    public ResilientWalletClient(WalletServiceClient walletServiceClient) {
        this.walletServiceClient = walletServiceClient;
    }

    @CircuitBreaker(name = "walletService", fallbackMethod = "balanceFallback")
    public BalanceResponse getBalance(Long userId) {
        return walletServiceClient.getBalance(userId);
    }

    public BalanceResponse balanceFallback(Long userId, Throwable t) {
        log.error("Wallet balance lookup failed for user {}: {}", userId, t.getMessage());
        throw new ServiceUnavailableException("Wallet service unavailable — could not fetch balance");
    }

    @CircuitBreaker(name = "walletService", fallbackMethod = "debitFallback")
    @Retry(name = "walletService")
    public WalletOperationResponse debit(Long userId, AmountRequest request) {
        return walletServiceClient.debit(userId, request);
    }

    public WalletOperationResponse debitFallback(Long userId, AmountRequest request, Throwable t) {
        log.error("Wallet debit failed for user {}: {}", userId, t.getMessage());
        throw new ServiceUnavailableException("Wallet service unavailable — sender could not be debited");
    }

    @CircuitBreaker(name = "walletService", fallbackMethod = "creditFallback")
    @Retry(name = "walletService")
    public WalletOperationResponse credit(Long userId, AmountRequest request) {
        return walletServiceClient.credit(userId, request);
    }

    public WalletOperationResponse creditFallback(Long userId, AmountRequest request, Throwable t) {
        log.error("Wallet credit failed for user {}: {}", userId, t.getMessage());
        throw new ServiceUnavailableException("Wallet service unavailable — receiver could not be credited");
    }
}
