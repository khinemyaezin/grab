package com.identity.domain.port.outbound;

import com.identity.domain.aggregate.Platform;

import java.util.Optional;
import java.util.Set;

public interface PlatformRepository {
    Optional<Platform> findByCode(String code);

    Set<Platform> findByRoleCode(String roleCode);

    Platform save(Platform platform);
}
