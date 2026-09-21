package com.payvault.user.service;

import com.payvault.user.client.WalletServiceClient;
import com.payvault.user.dto.*;
import com.payvault.user.entity.OtpRequest;
import com.payvault.user.entity.User;
import com.payvault.user.exception.UserServiceExceptions.*;
import com.payvault.user.repository.OtpRequestRepository;
import com.payvault.user.repository.UserRepository;
import com.payvault.user.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final OtpRequestRepository otpRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final WalletServiceClient walletServiceClient;

    public UserService(UserRepository userRepository,
                        OtpRequestRepository otpRequestRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil,
                        WalletServiceClient walletServiceClient) {
        this.userRepository = userRepository;
        this.otpRequestRepository = otpRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.walletServiceClient = walletServiceClient;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .isVerified(false)
                .build();

        User saved = userRepository.save(user);

        // Create the wallet for this user via Wallet Service. If Wallet Service is
        // temporarily down, we still let registration succeed and log it loudly —
        // in a full build this would be made resilient with a retry/outbox pattern.
        try {
            walletServiceClient.createWallet(new CreateWalletRequest(saved.getId()));
        } catch (Exception e) {
            log.error("Could not create wallet for user {}: {}", saved.getId(), e.getMessage());
        }

        return new MessageResponse("Registration successful. Please verify your account with the OTP sent to you.");
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail());
    }

    @Transactional
    public OtpGenerateResponse generateOtp(OtpGenerateRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        OtpRequest otp = OtpRequest.builder()
                .userId(request.userId())
                .otpCode(code)
                .purpose(request.purpose())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();
        otpRequestRepository.save(otp);

        // Dev mode: log it and hand it back instead of sending a real SMS/email.
        log.info("OTP for user {} ({}): {}", request.userId(), request.purpose(), code);

        return new OtpGenerateResponse("OTP sent (dev mode — see response/logs)", code);
    }

    @Transactional
    public MessageResponse verifyOtp(OtpVerifyRequest request) {
        OtpRequest otp = otpRequestRepository
                .findTopByUserIdAndPurposeOrderByIdDesc(request.userId(), request.purpose())
                .orElseThrow(() -> new InvalidOtpException("No OTP request found"));

        if (otp.isVerified()) {
            throw new InvalidOtpException("OTP already used");
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP expired");
        }
        if (!otp.getOtpCode().equals(request.otpCode())) {
            throw new InvalidOtpException("Incorrect OTP");
        }

        otp.setVerified(true);
        otpRequestRepository.save(otp);

        if ("REGISTRATION".equalsIgnoreCase(request.purpose())) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            user.setVerified(true);
            userRepository.save(user);
        }

        return new MessageResponse("OTP verified successfully");
    }

    @Transactional
    public MessageResponse setPin(Long userId, PinSetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPinHash(passwordEncoder.encode(request.pin()));
        userRepository.save(user);
        return new MessageResponse("Transaction PIN set successfully");
    }

    public PinVerifyResponse verifyPin(Long userId, PinVerifyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getPinHash() == null) {
            return new PinVerifyResponse(false);
        }
        boolean valid = passwordEncoder.matches(request.pin(), user.getPinHash());
        return new PinVerifyResponse(valid);
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
        }
        userRepository.save(user);
        return toProfileResponse(user);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.isVerified());
    }
}
