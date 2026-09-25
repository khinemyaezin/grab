package com.identity.domain.port.outbound;

import com.identity.domain.model.Authority;

import java.util.List;
import java.util.Set;

public interface AuthorityRepository {
    boolean existsByCode(String code);

    Set<String> findActiveCodes(Set<String> codes);

    void upsertAll(List<Authority> authorities);
}
