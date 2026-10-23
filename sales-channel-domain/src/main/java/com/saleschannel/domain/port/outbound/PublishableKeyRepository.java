package com.saleschannel.domain.port.outbound;

import com.grab.framework.id.Id;
import com.saleschannel.domain.aggregate.PublishableKey;

import java.util.Optional;

public interface PublishableKeyRepository {
    Optional<PublishableKey> findById(Id id);

    boolean existsByDigest(String digest);

    PublishableKey save(PublishableKey key);
}
