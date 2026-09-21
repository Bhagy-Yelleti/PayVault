package com.payvault.wallet.repository;

import com.payvault.wallet.entity.TopupRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopupRequestRepository extends JpaRepository<TopupRequest, Long> {
}
