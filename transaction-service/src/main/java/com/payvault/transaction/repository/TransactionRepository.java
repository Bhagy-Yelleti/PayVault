package com.payvault.transaction.repository;

import com.payvault.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    List<Transaction> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(Long senderId, Long receiverId);
    List<Transaction> findBySenderIdAndStatus(Long senderId, String status);
}
