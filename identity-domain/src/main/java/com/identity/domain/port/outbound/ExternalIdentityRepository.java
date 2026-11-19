package com.identity.domain.port.outbound;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.ExternalIdentity;

import java.util.Optional;

public interface ExternalIdentityRepository {
    Optional<ExternalIdentity> findByIssuerAndSubject(String issuer, String subject);

    Optional<ExternalIdentity> findByDirectoryIdAndResourceId(String directoryId, String resourceId);

    Optional<ExternalIdentity> findByUserId(Id userId);

    ExternalIdentity save(ExternalIdentity identity);
}
