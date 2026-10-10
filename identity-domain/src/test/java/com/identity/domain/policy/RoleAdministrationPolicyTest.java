package com.identity.domain.policy;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.port.outbound.AuthorityRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RoleAdministrationPolicyTest {

    @Test
    void createCustomRole_merchantAdmin_shouldReject() {
        AuthorityRepository authorities = mock(AuthorityRepository.class);
        RoleAdministrationPolicy policy = new RoleAdministrationPolicy(authorities);

        assertThatThrownBy(() -> policy.createCustomRole(
                new CommonId("role-1"),
                "MERCHANT_ADMIN",
                "Merchant Admin",
                "Custom admin",
                Set.of("CATALOG_READ")
        )).isInstanceOf(IdentityDomainValidationException.class)
                .hasMessageContaining("Cannot create custom role with reserved system role code");
    }
}
