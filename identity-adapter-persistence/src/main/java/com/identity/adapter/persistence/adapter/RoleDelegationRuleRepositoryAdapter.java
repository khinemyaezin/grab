package com.identity.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.port.outbound.RoleDelegationRuleRepository;
import com.identity.adapter.persistence.repository.jpa.RoleDelegationRuleJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class RoleDelegationRuleRepositoryAdapter implements RoleDelegationRuleRepository {
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
