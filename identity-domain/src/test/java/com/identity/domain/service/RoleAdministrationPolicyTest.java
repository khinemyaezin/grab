package com.identity.domain.service;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.aggregate.Role;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.aggregate.Authority;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleAdministrationPolicyTest {
    private final RoleAdministrationPolicy policy = new RoleAdministrationPolicy(
            new FixedAuthorityRepository(Set.of("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE"))
    );

    @Test
    void createCustomRole_withSupportedAuthorities_shouldCreateRole() {
        Role role = policy.createCustomRole(
                new CommonId("role-1"),
                "PROFILE_EDITOR",
                "Profile Editor",
                null,
                Set.of("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE")
        );

        assertThat(role.getKind()).isEqualTo(RoleKind.CUSTOM);
        assertThat(role.getAuthorities().stream().map(Authority::getCode).toList())
                .containsExactlyInAnyOrder("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE");
    }

    @Test
    void createCustomRole_withUnknownAuthority_shouldRejectRole() {
        assertThatThrownBy(() -> policy.createCustomRole(
                new CommonId("role-1"),
                "PROFILE_EDITOR",
                "Profile Editor",
                null,
                Set.of("UNKNOWN")
        )).isInstanceOf(IdentityDomainValidationException.class)
                .satisfies(exception -> assertThat(
                        ((IdentityDomainValidationException) exception).getMessageSource()
                ).isInstanceOf(IdentityDomainError.AuthoritiesUnavailable.class));
    }

    @Test
    void changeAuthority_withActiveAuthority_shouldAssignAndRevoke() {
        Role role = policy.createCustomRole(
                new CommonId("role-1"),
                "PROFILE_EDITOR",
                "Profile Editor",
                null,
                Set.of("MERCHANT_PROFILE_READ")
        );

        policy.changeAuthority(role, "MERCHANT_PROFILE_WRITE", true);
        assertThat(role.getAuthorities().stream().map(Authority::getCode).toList()).contains("MERCHANT_PROFILE_WRITE");

        policy.changeAuthority(role, "MERCHANT_PROFILE_WRITE", false);
        assertThat(role.getAuthorities().stream().map(Authority::getCode).toList()).doesNotContain("MERCHANT_PROFILE_WRITE");
    }

    private record FixedAuthorityRepository(Set<String> activeCodes) implements AuthorityRepository {
        @Override
        public Set<Authority> findActiveByCodes(Set<String> codes) {
            return activeCodes.stream()
                    .filter(codes::contains)
                    .map(code -> Authority.rehydrate(
                            new CommonId("authority-" + code), code, "identity", code, null, true
                    ))
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }

        @Override
        public void upsertAll(List<Authority> authorities) {
            throw new UnsupportedOperationException("Not used by role administration policy tests");
        }
    }
}
