package com.identity.domain.service;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.repository.RoleDelegationRuleRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuleBasedRoleDelegationPolicyTest {
    private final RoleDelegationRuleRepository rules = (delegators, delegated) ->
            delegators.contains("ROLE_A") && delegated.equals("ROLE_B");
    private final RoleDelegationPolicy policy = new RuleBasedRoleDelegationPolicy(rules);

    @Test
    void requireCanDelegate_withConfiguredRule_shouldAllowDelegation() {
        assertDoesNotThrow(() -> policy.requireCanDelegate(Set.of(" role_a "), " role_b "));
    }

    @Test
    void requireCanDelegate_withoutConfiguredRule_shouldDenyDelegation() {
        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.requireCanDelegate(Set.of("ROLE_A"), "ROLE_C")
        );

        assertInstanceOf(IdentityDomainError.AccessRoleDelegationForbidden.class,
                exception.getMessageSource());
    }

    @Test
    void requireCanDelegate_withoutActorRoles_shouldDenyDelegation() {
        assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.requireCanDelegate(Set.of(), "ROLE_B")
        );
    }
}
