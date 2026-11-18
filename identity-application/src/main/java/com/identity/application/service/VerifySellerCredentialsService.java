package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.model.read.CredentialView;
import com.identity.application.model.read.VerifiedSeller;
import com.identity.application.model.read.VerifySellerCredentialsQuery;
import com.identity.application.port.inbound.VerifySellerCredentialsUseCase;
import com.identity.application.port.outbound.IdentityLookupQueryPort;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public final class VerifySellerCredentialsService implements VerifySellerCredentialsUseCase {
    private final IdentityLookupQueryPort identities;
    private final PasswordHasher passwords;
    private final IdGenerator ids;
    private final HashedPassword dummyHash;

    public VerifySellerCredentialsService(IdentityLookupQueryPort identities, PasswordHasher passwords, IdGenerator ids) {
        this.identities = identities;
        this.passwords = passwords;
        this.ids = ids;
        UUID random = UUID.randomUUID();
        dummyHash = passwords.hash(random.toString());
    }

    @Override
    public VerifiedSeller execute(VerifySellerCredentialsQuery query) {
        String supplied = query.password();
        if (supplied == null || supplied.getBytes(StandardCharsets.UTF_8).length > 72) {
            return null;
        }
        Optional<CredentialView> credentials = findCredentials(query.email());
        CredentialView user = credentials.orElse(null);
        boolean hasHash = user != null && user.passwordHash() != null;
        HashedPassword hash = hasHash ? new HashedPassword(user.passwordHash()) : dummyHash;
        boolean matches = passwords.verify(supplied, hash);
        if (!matches || !hasHash || user.status() != UserStatus.ACTIVE) {
            return null;
        }
        Id id = ids.convertIdFrom(user.userId());
        return new VerifiedSeller(id, user.authenticationVersion());
    }

    private Optional<CredentialView> findCredentials(String supplied) {
        try {
            Email email = new Email(supplied);
            return identities.findCredentials(email.value());
        } catch (IdentityDomainValidationException exception) {
            return Optional.empty();
        }
    }
}
