package com.identity.domain.port.outbound;

import java.util.Set;

public interface RoleDelegationRuleRepository {
    boolean existsActiveRule(Set<String> delegatorRoleCodes, String delegatedRoleCode);
}
