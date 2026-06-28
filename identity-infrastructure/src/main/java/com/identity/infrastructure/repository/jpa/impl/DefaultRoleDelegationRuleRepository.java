package com.identity.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.repository.RoleDelegationRuleRepository;
import com.identity.infrastructure.repository.jpa.RoleDelegationRuleJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class DefaultRoleDelegationRuleRepository implements RoleDelegationRuleRepository {
    private final RoleDelegationRuleJpaRepository rules;
    private final PersistenceExecutor executor;

    @Override
    public boolean existsActiveRule(Set<String> delegatorRoleCodes, String delegatedRoleCode) {
        if (delegatorRoleCodes.isEmpty()) {
            return false;
        }
        return executor.query(
                "RoleDelegationRule",
                () -> rules.existsActiveRule(delegatorRoleCodes, delegatedRoleCode)
        );
    }
}
