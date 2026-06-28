package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.RoleDelegationRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RoleDelegationRuleJpaRepository extends JpaRepository<RoleDelegationRuleEntity, Long> {
    @Query("""
            select (count(rule) > 0) from RoleDelegationRuleEntity rule
            where rule.delegatorRole.code in :delegatorRoleCodes
              and rule.delegatedRole.code = :delegatedRoleCode
              and rule.delegatorRole.active = true
              and rule.delegatedRole.active = true
            """)
    boolean existsActiveRule(
            @Param("delegatorRoleCodes") Set<String> delegatorRoleCodes,
            @Param("delegatedRoleCode") String delegatedRoleCode
    );
}
