package com.identity.domain.repository;

import com.identity.domain.aggregate.Role;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository {
    Optional<Role> findByCode(String code);

    Set<Role> findByCodes(Set<String> codes);

    Role save(Role role);
}
