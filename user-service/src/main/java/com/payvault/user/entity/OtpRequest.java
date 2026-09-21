package com.payvault.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private String purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private boolean verified;
}
