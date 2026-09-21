package com.payvault.wallet.controller;

import com.payvault.wallet.dto.*;
import com.payvault.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // Called internally by User Service right after registration.
    @PostMapping
    public MessageResponse createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return walletService.createWallet(request);
    }

    @GetMapping("/{userId}/balance")
    public BalanceResponse getBalance(@PathVariable Long userId) {
        return walletService.getBalance(userId);
    }

    @PostMapping("/{userId}/credit")
    public WalletOperationResponse credit(@PathVariable Long userId, @Valid @RequestBody AmountRequest request) {
        return walletService.credit(userId, request);
    }

    @PostMapping("/{userId}/debit")
    public WalletOperationResponse debit(@PathVariable Long userId, @Valid @RequestBody AmountRequest request) {
        return walletService.debit(userId, request);
    }

    @PostMapping("/{userId}/topup")
    public WalletOperationResponse topup(@PathVariable Long userId, @Valid @RequestBody TopupRequestDto request) {
        return walletService.topup(userId, request);
    }
}
