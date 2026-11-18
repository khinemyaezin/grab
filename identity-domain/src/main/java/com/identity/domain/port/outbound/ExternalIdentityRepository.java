package com.identity.domain.port.outbound;

import com.identity.domain.aggregate.ExternalIdentity;

import java.util.Optional;

public interface ExternalIdentityRepository {
    Optional<ExternalIdentity> findByIssuerAndSubject(String issuer, String subject);

    Optional<ExternalIdentity> findByDirectoryIdAndResourceId(String directoryId, String resourceId);

    ExternalIdentity save(ExternalIdentity identity);
}
