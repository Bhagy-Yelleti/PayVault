package com.payvault.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @PostMapping("/api/users/{id}/pin/verify")
    PinVerifyResponse verifyPin(@PathVariable("id") Long id, @RequestBody PinVerifyRequest request);

    @GetMapping("/api/users/{id}")
    UserProfileResponse getUser(@PathVariable("id") Long id);
}
