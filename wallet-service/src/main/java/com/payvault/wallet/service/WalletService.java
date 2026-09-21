package com.payvault.wallet.service;

import com.payvault.wallet.dto.*;
import com.payvault.wallet.entity.TopupRequest;
import com.payvault.wallet.entity.Wallet;
import com.payvault.wallet.exception.WalletServiceExceptions.*;
import com.payvault.wallet.repository.TopupRequestRepository;
import com.payvault.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TopupRequestRepository topupRequestRepository;

    public WalletService(WalletRepository walletRepository, TopupRequestRepository topupRequestRepository) {
        this.walletRepository = walletRepository;
        this.topupRequestRepository = topupRequestRepository;
    }

    @Transactional
    public MessageResponse createWallet(CreateWalletRequest request) {
        if (walletRepository.existsByUserId(request.userId())) {
            throw new WalletAlreadyExistsException("Wallet already exists for this user");
        }
        Wallet wallet = Wallet.builder()
                .userId(request.userId())
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .build();
        walletRepository.save(wallet);
        return new MessageResponse("Wallet created successfully");
    }

    public BalanceResponse getBalance(Long userId) {
        Wallet wallet = findByUserId(userId);
        return new BalanceResponse(userId, wallet.getBalance(), wallet.getCurrency());
    }

    @Transactional
    public WalletOperationResponse credit(Long userId, AmountRequest request) {
        Wallet wallet = findByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(request.amount()));
        walletRepository.save(wallet); // @Version guards this against concurrent writes
        return new WalletOperationResponse(userId, wallet.getBalance(), "SUCCESS");
    }

    @Transactional
    public WalletOperationResponse debit(Long userId, AmountRequest request) {
        Wallet wallet = findByUserId(userId);
        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in wallet");
        }
        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        walletRepository.save(wallet);
        return new WalletOperationResponse(userId, wallet.getBalance(), "SUCCESS");
    }

    @Transactional
    public WalletOperationResponse topup(Long userId, TopupRequestDto request) {
        Wallet wallet = findByUserId(userId);

        // Simulated payment gateway — always succeeds. A real UPI/card/bank integration
        // would go here; the top-up API shape already anticipates a real one.
        TopupRequest topup = TopupRequest.builder()
                .walletId(wallet.getId())
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .status("SUCCESS")
                .build();
        topupRequestRepository.save(topup);

        wallet.setBalance(wallet.getBalance().add(request.amount()));
        walletRepository.save(wallet);

        return new WalletOperationResponse(userId, wallet.getBalance(), "SUCCESS");
    }

    private Wallet findByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("No wallet found for this user"));
    }
}
