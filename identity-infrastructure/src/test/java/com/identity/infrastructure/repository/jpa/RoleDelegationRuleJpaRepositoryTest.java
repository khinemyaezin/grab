package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.RoleDelegationRuleEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleDelegationRuleJpaRepositoryTest extends RepositoryTestConfig {
    @Autowired
    private RoleDelegationRuleJpaRepository rules;

    @Autowired
    private RoleJpaRepository roles;

    private RoleEntity delegator;
    private RoleEntity delegated;

    @BeforeEach
    void setUp() {
        rules.deleteAll();
        roles.deleteAll();
        delegator = role("role-a", "ROLE_A", true);
        delegated = role("role-b", "ROLE_B", true);
        roles.saveAll(List.of(delegator, delegated));

        RoleDelegationRuleEntity rule = new RoleDelegationRuleEntity();
        rule.setDelegatorRole(delegator);
        rule.setDelegatedRole(delegated);
        rules.save(rule);
    }

    @Test
    void canDelegate_withActiveConfiguredRoles_shouldReturnTrue() {
        assertThat(rules.existsActiveRule(Set.of("ROLE_A"), "ROLE_B")).isTrue();
    }

    @Test
    void canDelegate_withoutConfiguredRule_shouldReturnFalse() {
        assertThat(rules.existsActiveRule(Set.of("ROLE_B"), "ROLE_A")).isFalse();
    }

    @Test
    void canDelegate_withInactiveDelegatedRole_shouldReturnFalse() {
        delegated.setActive(false);
        roles.save(delegated);

        assertThat(rules.existsActiveRule(Set.of("ROLE_A"), "ROLE_B")).isFalse();
    }

    private RoleEntity role(String uuid, String code, boolean active) {
        RoleEntity role = new RoleEntity();
        role.setUuid(uuid);
        role.setCode(code);
        role.setName(code);
        role.setActive(active);
        return role;
    }
}
