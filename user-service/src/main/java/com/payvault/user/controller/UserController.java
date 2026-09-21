package com.payvault.user.controller;

import com.payvault.user.dto.*;
import com.payvault.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/otp/generate")
    public OtpGenerateResponse generateOtp(@Valid @RequestBody OtpGenerateRequest request) {
        return userService.generateOtp(request);
    }

    @PostMapping("/otp/verify")
    public MessageResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return userService.verifyOtp(request);
    }

    @PostMapping("/{id}/pin")
    public MessageResponse setPin(@PathVariable Long id, @Valid @RequestBody PinSetRequest request) {
        return userService.setPin(id, request);
    }

    @PostMapping("/{id}/pin/verify")
    public PinVerifyResponse verifyPin(@PathVariable Long id, @Valid @RequestBody PinVerifyRequest request) {
        return userService.verifyPin(id, request);
    }

    @GetMapping("/{id}")
    public UserProfileResponse getProfile(@PathVariable Long id) {
        return userService.getProfile(id);
    }

    @PutMapping("/{id}")
    public UserProfileResponse updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(id, request);
    }
}
