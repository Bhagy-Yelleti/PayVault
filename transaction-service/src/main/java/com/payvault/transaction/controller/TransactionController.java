package com.payvault.transaction.controller;

import com.payvault.transaction.dto.*;
import com.payvault.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/validate")
    public ValidateResponse validate(@Valid @RequestBody ValidateRequest request) {
        return transactionService.validate(request);
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }

    @GetMapping("/{id}/status")
    public StatusResponse getStatus(@PathVariable Long id) {
        return transactionService.getStatus(id);
    }

    @GetMapping("/{id}")
    public Object getTransaction(@PathVariable Long id) {
        return transactionService.getById(id);
    }

    @GetMapping("/{userId}/history")
    public List<HistoryItem> getHistory(@PathVariable Long userId) {
        return transactionService.getHistory(userId);
    }

    @GetMapping("/{userId}/summary")
    public SummaryResponse getSummary(@PathVariable Long userId) {
        return transactionService.getSummary(userId);
    }
}
