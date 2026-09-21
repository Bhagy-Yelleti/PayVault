package com.payvault.transaction.service;

import com.payvault.transaction.client.PinVerifyRequest;
import com.payvault.transaction.client.PinVerifyResponse;
import com.payvault.transaction.client.UserProfileResponse;
import com.payvault.transaction.client.UserServiceClient;
import com.payvault.transaction.exception.TransactionServiceExceptions.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResilientUserClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientUserClient.class);

    private final UserServiceClient userServiceClient;

    public ResilientUserClient(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "verifyPinFallback")
    @Retry(name = "userService")
    public PinVerifyResponse verifyPin(Long userId, PinVerifyRequest request) {
        return userServiceClient.verifyPin(userId, request);
    }

    public PinVerifyResponse verifyPinFallback(Long userId, PinVerifyRequest request, Throwable t) {
        log.error("PIN verification failed for user {}: {}", userId, t.getMessage());
        throw new ServiceUnavailableException("User service unavailable — could not verify PIN");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    public UserProfileResponse getUser(Long userId) {
        return userServiceClient.getUser(userId);
    }

    public UserProfileResponse getUserFallback(Long userId, Throwable t) {
        log.error("User lookup failed for user {}: {}", userId, t.getMessage());
        throw new ServiceUnavailableException("User service unavailable — could not verify receiver");
    }
}
