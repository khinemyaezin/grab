package com.identity.domain.repository;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.User;
import com.identity.domain.valueobject.Email;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Id id);
    Optional<User> findByEmail(Email email);
    boolean existsByEmail(Email email);
    User save(User user);
}
