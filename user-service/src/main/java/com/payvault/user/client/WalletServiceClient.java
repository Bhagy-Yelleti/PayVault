package com.payvault.user.client;

import com.payvault.user.dto.CreateWalletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Resolved via Eureka by service name — no hardcoded host/port.
@FeignClient(name = "wallet-service")
public interface WalletServiceClient {

    @PostMapping("/api/wallets")
    void createWallet(@RequestBody CreateWalletRequest request);
}
