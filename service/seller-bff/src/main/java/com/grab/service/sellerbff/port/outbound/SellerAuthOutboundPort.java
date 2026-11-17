package com.grab.service.sellerbff.port.outbound;

public interface SellerAuthOutboundPort {
    SellerUserRegistrationResult registerUser(String email, String password);

    record SellerUserRegistrationResult(
            String id,
            String email,
            String status,
            String createdAt
    ) {}
}

