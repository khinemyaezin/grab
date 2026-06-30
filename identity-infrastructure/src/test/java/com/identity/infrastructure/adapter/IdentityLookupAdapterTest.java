package com.identity.infrastructure.adapter;

import com.grab.framework.security.AccessContext;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.UserStatus;
import com.identity.infrastructure.entity.AccessAssignmentEntity;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.entity.PlatformEntity;
import com.identity.infrastructure.entity.PlatformRoleEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.AccessAssignmentJpaRepository;
import com.identity.infrastructure.repository.jpa.ExternalEntitlementMappingJpaRepository;
import com.identity.infrastructure.repository.jpa.ExternalIdentityJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityLookupAdapterTest {

    @Mock
    private UserJpaRepository users;
    @Mock
    private ExternalIdentityJpaRepository externalIdentities;
    @Mock
    private ExternalEntitlementMappingJpaRepository entitlementMappings;
    @Mock
    private AccessAssignmentJpaRepository assignments;

    @Test
    void resolveByPlatformUserId_shouldCombineEffectiveRolesForSelectedScope() {
        UserEntity user = new UserEntity();
        user.setUuid("user-1");
        user.setEmail("owner@example.com");
        user.setStatus(UserStatus.ACTIVE);

        AccessAssignmentEntity owner = assignment(
                "assignment-1", user, "MERCHANT_OWNER", "MERCHANT_WRITE", "merchant-1"
        );
        AccessAssignmentEntity manager = assignment(
                "assignment-2", user, "STORE_MANAGER", "INVENTORY_WRITE", "merchant-1"
        );
        AccessAssignmentEntity otherMerchant = assignment(
                "assignment-3", user, "MERCHANT_OWNER", "MERCHANT_WRITE", "merchant-2"
        );

        when(users.findByUuid("user-1")).thenReturn(Optional.of(user));
        when(assignments.findForContext("assignment-1", "user-1", "SELLER_PORTAL"))
                .thenReturn(Optional.of(owner));
        when(assignments.findEffectiveByUserAndPlatform(
                eq("user-1"), eq("SELLER_PORTAL"), any(Instant.class)
        )).thenReturn(List.of(owner, manager, otherMerchant));

        var actor = new IdentityLookupAdapter(
                users, externalIdentities, entitlementMappings, assignments
        ).resolveByPlatformUserId(
                "local-issuer",
                "user-1",
                new AccessContext(
                        "SELLER_PORTAL", "assignment-1", "merchant.account", "merchant-1"
                )
        ).orElseThrow();

        assertThat(actor.roles()).containsExactlyInAnyOrder("MERCHANT_OWNER", "STORE_MANAGER");
        assertThat(actor.authorities()).containsExactlyInAnyOrder("MERCHANT_WRITE", "INVENTORY_WRITE");
    }

    private AccessAssignmentEntity assignment(
            String id,
            UserEntity user,
            String roleCode,
            String authorityCode,
            String scopeId
    ) {
        AuthorityEntity authority = new AuthorityEntity();
        authority.setCode(authorityCode);
        authority.setName(authorityCode);
        authority.setActive(true);

        RoleEntity role = new RoleEntity();
        role.setUuid("role-" + roleCode);
        role.setCode(roleCode);
        role.setName(roleCode);
        role.setActive(true);
        role.setAuthorities(Set.of(authority));

        PlatformEntity platform = new PlatformEntity();
        platform.setUuid("seller-platform");
        platform.setCode("SELLER_PORTAL");
        platform.setName("Seller Portal");
        platform.setActive(true);

        PlatformRoleEntity platformRole = new PlatformRoleEntity();
        platformRole.setUuid("platform-role-" + roleCode);
        platformRole.setPlatform(platform);
        platformRole.setRole(role);
        platformRole.setActive(true);

        AccessAssignmentEntity assignment = new AccessAssignmentEntity();
        assignment.setUuid(id);
        assignment.setUser(user);
        assignment.setPlatformRole(platformRole);
        assignment.setScopeKey("merchant.account");
        assignment.setScopeId(scopeId);
        assignment.setStatus(AccessAssignmentStatus.ACTIVE);
        assignment.setCreatedAt(Instant.now());
        assignment.setUpdatedAt(Instant.now());
        return assignment;
    }
}
