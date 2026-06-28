package com.identity.domain.repository;

import com.identity.domain.aggregate.Platform;

import java.util.Optional;

public interface PlatformRepository {
    Optional<Platform> findByCode(String code);
}
