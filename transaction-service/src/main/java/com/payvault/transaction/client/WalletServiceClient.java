package com.payvault.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service", configuration = FeignClientConfig.class)
public interface WalletServiceClient {

    @GetMapping("/api/wallets/{userId}/balance")
    BalanceResponse getBalance(@PathVariable("userId") Long userId);

    @PostMapping("/api/wallets/{userId}/debit")
    WalletOperationResponse debit(@PathVariable("userId") Long userId, @RequestBody AmountRequest request);

    @PostMapping("/api/wallets/{userId}/credit")
    WalletOperationResponse credit(@PathVariable("userId") Long userId, @RequestBody AmountRequest request);
}
