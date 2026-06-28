package com.identity.domain.repository;

import java.util.Set;

public interface RoleDelegationRuleRepository {
    boolean existsActiveRule(Set<String> delegatorRoleCodes, String delegatedRoleCode);
}
