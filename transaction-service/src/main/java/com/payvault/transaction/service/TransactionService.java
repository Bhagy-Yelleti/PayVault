package com.payvault.transaction.service;

import com.payvault.transaction.client.*;
import com.payvault.transaction.dto.*;
import com.payvault.transaction.entity.LedgerEntry;
import com.payvault.transaction.entity.Transaction;
import com.payvault.transaction.exception.TransactionServiceExceptions.*;
import com.payvault.transaction.repository.LedgerEntryRepository;
import com.payvault.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final ResilientWalletClient walletClient;
    private final ResilientUserClient userClient;

    public TransactionService(TransactionRepository transactionRepository,
                               LedgerEntryRepository ledgerEntryRepository,
                               ResilientWalletClient walletClient,
                               ResilientUserClient userClient) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.walletClient = walletClient;
        this.userClient = userClient;
    }

    // Preview step for the "Review Transfer" screen — no money moves here.
    public ValidateResponse validate(ValidateRequest request) {
        if (request.senderId().equals(request.receiverId())) {
            return new ValidateResponse(false, "You cannot transfer money to yourself", null);
        }

        try {
            userClient.getUser(request.receiverId());
        } catch (Exception e) {
            return new ValidateResponse(false, "Receiver not found", null);
        }

        BalanceResponse balance = walletClient.getBalance(request.senderId());
        if (balance.balance().compareTo(request.amount()) < 0) {
            return new ValidateResponse(false, "Insufficient balance", balance.balance());
        }

        return new ValidateResponse(true, "Transfer looks good", balance.balance());
    }

    // The saga: verify PIN -> debit sender -> credit receiver -> write ledger.
    // Any failure after the debit triggers a compensating credit back to the sender.
    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        // Idempotency: if this exact request was already processed, return the same result
        // instead of transferring the money a second time.
        var existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            Transaction t = existing.get();
            return new TransferResponse(t.getId(), t.getStatus(), "Duplicate request — returning original result");
        }

        if (request.senderId().equals(request.receiverId())) {
            throw new InvalidTransferException("You cannot transfer money to yourself");
        }

        PinVerifyResponse pinCheck = userClient.verifyPin(request.senderId(), new PinVerifyRequest(request.pin()));
        if (!pinCheck.valid()) {
            throw new InvalidPinException("Incorrect transaction PIN");
        }

        Transaction transaction = Transaction.builder()
                .senderId(request.senderId())
                .receiverId(request.receiverId())
                .amount(request.amount())
                .category(request.category() != null ? request.category() : "Other")
                .status("PENDING")
                .idempotencyKey(request.idempotencyKey())
                .build();
        transaction = transactionRepository.save(transaction);

        String reference = "txn-" + transaction.getId();

        // Step 1: debit sender
        WalletOperationResponse debitResult;
        try {
            debitResult = walletClient.debit(request.senderId(), new AmountRequest(request.amount(), reference));
        } catch (Exception e) {
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            return new TransferResponse(transaction.getId(), "FAILED", "Debit failed: " + e.getMessage());
        }

        // Step 2: credit receiver
        WalletOperationResponse creditResult;
        try {
            creditResult = walletClient.credit(request.receiverId(), new AmountRequest(request.amount(), reference));
        } catch (Exception e) {
            // Compensating transaction: give the sender their money back.
            try {
                walletClient.credit(request.senderId(), new AmountRequest(request.amount(), reference + "-rollback"));
                log.warn("Compensating rollback applied for transaction {}", transaction.getId());
            } catch (Exception rollbackError) {
                log.error("CRITICAL: rollback also failed for transaction {}: {}", transaction.getId(), rollbackError.getMessage());
            }
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            return new TransferResponse(transaction.getId(), "FAILED", "Credit failed, sender refunded: " + e.getMessage());
        }

        // Step 3: double-entry ledger
        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionId(transaction.getId())
                .accountId(request.senderId())
                .entryType("DEBIT")
                .amount(request.amount())
                .balanceAfter(debitResult.newBalance())
                .build());

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionId(transaction.getId())
                .accountId(request.receiverId())
                .entryType("CREDIT")
                .amount(request.amount())
                .balanceAfter(creditResult.newBalance())
                .build());

        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        return new TransferResponse(transaction.getId(), "SUCCESS", "Transfer completed successfully");
    }

    public StatusResponse getStatus(Long transactionId) {
        Transaction t = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
        return new StatusResponse(t.getId(), t.getStatus());
    }

    public Transaction getById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    public List<HistoryItem> getHistory(Long userId) {
        return transactionRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(t -> new HistoryItem(t.getId(), t.getSenderId(), t.getReceiverId(), t.getAmount(),
                        t.getCategory(), t.getStatus(), t.getCreatedAt()))
                .toList();
    }

    public SummaryResponse getSummary(Long userId) {
        List<Transaction> sent = transactionRepository.findBySenderIdAndStatus(userId, "SUCCESS");

        Map<String, BigDecimal> totals = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Transaction t : sent) {
            String category = t.getCategory() != null ? t.getCategory() : "Other";
            totals.merge(category, t.getAmount(), BigDecimal::add);
            total = total.add(t.getAmount());
        }

        return new SummaryResponse(totals, total);
    }
}
