package com.identity.domain.service;

import java.util.Set;

public interface RoleDelegationPolicy {
    void requireCanDelegate(Set<String> actorRoleCodes, String requestedRoleCode);
}
