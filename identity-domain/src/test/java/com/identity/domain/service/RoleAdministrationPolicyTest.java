package com.identity.domain.service;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import com.identity.domain.repository.AuthorityRepository;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleAdministrationPolicyTest {
    private final RoleAdministrationPolicy policy = new RoleAdministrationPolicy(
            new FixedAuthorityRepository(Set.of("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE"))
    );

    @Test
    void createCustomRole_withSupportedAuthorities_shouldBindRoleToPlatform() {
        Platform platform = sellerPlatform(Set.of("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE"));

        Role role = policy.createCustomRole(
                new CommonId("role-1"),
                "PROFILE_EDITOR",
                "Profile Editor",
                null,
                platform,
                Set.of("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE")
        );

        assertThat(role.getKind()).isEqualTo(RoleKind.CUSTOM);
        assertThat(role.getAuthorityCodes())
                .containsExactlyInAnyOrder("MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE");
        assertThat(platform.getRoleCodes()).contains("PROFILE_EDITOR");
    }

    @Test
    void createCustomRole_withAuthorityOutsidePlatform_shouldRejectRole() {
        Platform platform = sellerPlatform(Set.of("MERCHANT_PROFILE_READ"));

        assertThatThrownBy(() -> policy.createCustomRole(
                new CommonId("role-1"),
                "PROFILE_EDITOR",
                "Profile Editor",
                null,
                platform,
                Set.of("MERCHANT_PROFILE_WRITE")
        )).isInstanceOf(IdentityDomainValidationException.class)
                .satisfies(exception -> assertThat(
                        ((IdentityDomainValidationException) exception).getMessageSource()
                ).isInstanceOf(IdentityDomainError.PlatformAuthorityNotSupported.class));
    }

    @Test
    void createCustomRole_withUnknownAuthority_shouldRejectRole() {
        Platform platform = sellerPlatform(Set.of("UNKNOWN"));

        assertThatThrownBy(() -> policy.createCustomRole(
                new CommonId("role-1"),
                "PROFILE_EDITOR",
                "Profile Editor",
                null,
                platform,
                Set.of("UNKNOWN")
        )).isInstanceOf(IdentityDomainValidationException.class)
                .satisfies(exception -> assertThat(
                        ((IdentityDomainValidationException) exception).getMessageSource()
                ).isInstanceOf(IdentityDomainError.AuthoritiesUnavailable.class));
    }

    private Platform sellerPlatform(Set<String> authorityCodes) {
        return new Platform(
                new CommonId("seller-platform"),
                "SELLER_PORTAL",
                "Seller Portal",
                true,
                Set.of(),
                authorityCodes
        );
    }

    private record FixedAuthorityRepository(Set<String> activeCodes) implements AuthorityRepository {
        @Override
        public boolean existsByCode(String code) {
            return activeCodes.contains(code);
        }

        @Override
        public Set<String> findActiveCodes(Set<String> codes) {
            LinkedHashSet<String> found = new LinkedHashSet<>(codes);
            found.retainAll(activeCodes);
            return Set.copyOf(found);
        }
    }
}
