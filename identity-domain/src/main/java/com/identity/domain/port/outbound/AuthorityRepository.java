package com.identity.domain.port.outbound;

import java.util.Set;

public interface AuthorityRepository {
    boolean existsByCode(String code);

    Set<String> findActiveCodes(Set<String> codes);
}
