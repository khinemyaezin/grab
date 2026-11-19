package com.grab.service.authorizationserver.core.user;

import java.util.Optional;

public interface IamUserRepository {
    Optional<IamUser> findById(String id);

    Optional<IamUser> findByEmail(String email);

    Optional<IamUser> findByUsername(String username);

    Optional<IamUser> findByUsernameOrEmail(String identifier);

    IamUser save(IamUser user);

    void updatePlatformUserId(String id, String platformUserId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
