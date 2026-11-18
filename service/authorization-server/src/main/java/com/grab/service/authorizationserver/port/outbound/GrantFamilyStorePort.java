package com.grab.service.authorizationserver.port.outbound;

import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.util.function.Supplier;

public interface GrantFamilyStorePort {
    <T> T inTransaction(Supplier<T> work);

    boolean lock(String familyId);

    void consume(String familyId, OAuth2RefreshToken token);

    String findConsumedFamily(String token);

    void cleanExpired();
}
