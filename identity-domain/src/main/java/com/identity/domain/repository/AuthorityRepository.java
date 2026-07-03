package com.identity.domain.repository;

import java.util.Set;

public interface AuthorityRepository {
    boolean existsByCode(String code);

    Set<String> findActiveCodes(Set<String> codes);
}
