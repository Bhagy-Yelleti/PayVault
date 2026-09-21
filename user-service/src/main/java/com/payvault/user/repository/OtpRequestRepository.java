package com.payvault.user.repository;

import com.payvault.user.entity.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {
    Optional<OtpRequest> findTopByUserIdAndPurposeOrderByIdDesc(Long userId, String purpose);
}
