package com.payvault.transaction.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Copies the caller's JWT onto every outgoing Feign call, so User Service and
// Wallet Service can validate the same token Transaction Service received.
@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor authHeaderInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader("Authorization");
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                }
            }
        };
    }
}
