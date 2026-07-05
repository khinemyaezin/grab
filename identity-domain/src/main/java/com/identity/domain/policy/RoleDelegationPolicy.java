package com.identity.domain.policy;

import java.util.Set;

public interface RoleDelegationPolicy {
    void requireCanDelegate(Set<String> actorRoleCodes, String requestedRoleCode);
}
