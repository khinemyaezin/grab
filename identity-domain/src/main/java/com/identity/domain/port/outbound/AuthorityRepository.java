package com.identity.domain.port.outbound;

import com.identity.domain.aggregate.Authority;

import java.util.List;
import java.util.Set;

public interface AuthorityRepository {
    Set<Authority> findActiveByCodes(Set<String> codes);

    default Set<String> findCodesByModule(String moduleKey) {
        return Set.of();
    }

    default Set<String> findCodesOwnedByOtherModules(String moduleKey) {
        return Set.of();
    }

    void upsertAll(List<Authority> authorities);

    /** Retires provider definitions omitted from a complete module snapshot. */
    default void retireMissing(String moduleKey, Set<String> declaredCodes) {
        // Legacy adapters may not support provider lifecycle metadata yet.
    }

    default void retireCodes(String moduleKey, Set<String> codes) {
        // Legacy adapters may not support explicit lifecycle tombstones yet.
    }
}
