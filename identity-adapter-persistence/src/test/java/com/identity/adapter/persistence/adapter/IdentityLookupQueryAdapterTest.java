package com.identity.adapter.persistence.adapter;

import com.grab.framework.security.AccessContext;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.UserStatus;
import com.identity.adapter.persistence.entity.AccessAssignmentEntity;
import com.identity.adapter.persistence.entity.AuthorityEntity;
import com.identity.adapter.persistence.entity.RoleEntity;
import com.identity.adapter.persistence.entity.UserEntity;
import com.identity.adapter.persistence.repository.jpa.AccessAssignmentJpaRepository;
import com.identity.adapter.persistence.repository.jpa.ExternalEntitlementMappingJpaRepository;
import com.identity.adapter.persistence.repository.jpa.ExternalIdentityJpaRepository;
import com.identity.adapter.persistence.repository.jpa.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import com.identity.application.model.read.ScopeCatalogView;
import com.identity.application.port.outbound.ScopeCatalogQueryPort;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityLookupQueryAdapterTest {

    @Mock
    private UserJpaRepository users;
    @Mock
    private ExternalIdentityJpaRepository externalIdentities;
    @Mock
    private ExternalEntitlementMappingJpaRepository entitlementMappings;
    @Mock
    private AccessAssignmentJpaRepository assignments;

    @Test
    void resolveByPlatformUserId_effectiveRolesForSelectedScope_combinesRolesAndAuthorities() {
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
        when(assignments.findByUuidAndUser_Uuid("assignment-1", "user-1"))
                .thenReturn(Optional.of(owner));
        when(assignments.findEffectiveByUser(
                eq("user-1"), any(Instant.class)
        )).thenReturn(List.of(owner, manager, otherMerchant));

        var actor = new IdentityLookupQueryAdapter(
                users, externalIdentities, entitlementMappings, assignments,
                () -> new ScopeCatalogView(Collections.singletonMap("merchant.account", null),
                        Map.of("merchant.account", "merchant"), Set.of())
        ).resolveByPlatformUserId(
                "local-issuer",
                "user-1",
                new AccessContext(
                        "assignment-1", "merchant.account", "merchant-1"
                )
        ).orElseThrow();

        assertThat(actor.roles()).containsExactlyInAnyOrder("MERCHANT_OWNER", "STORE_MANAGER");
        assertThat(actor.authorities()).containsExactlyInAnyOrder("MERCHANT_WRITE", "INVENTORY_WRITE");
    }

    @Test
    void resolve_retiredAncestor_rejectsSelectedDescendantBeforeAssignmentLookup() {
        var user = new UserEntity();
        user.setUuid("user-1");
        user.setStatus(UserStatus.ACTIVE);
        when(users.findByUuid("user-1")).thenReturn(Optional.of(user));
        var parents = new java.util.HashMap<String, String>();
        parents.put("merchant.account", null);
        parents.put("inventory.location", "merchant.account");
        var view = new ScopeCatalogView(parents, Map.of("merchant.account", "merchant", "inventory.location", "inventory"), Set.of("merchant.account"));
        var adapter = new IdentityLookupQueryAdapter(users, externalIdentities, entitlementMappings, assignments, () -> view);
        var context = new AccessContext("assignment-1", "inventory.location", "location-1");
        assertThatThrownBy(() -> adapter.resolveByPlatformUserId("local", "user-1", context))
                .isInstanceOf(com.identity.domain.exception.IdentityDomainValidationException.class);
    }

    @Test
    void resolve_unknownScope_failsClosed() {
        var user = new UserEntity();
        user.setUuid("user-1");
        user.setStatus(UserStatus.ACTIVE);
        when(users.findByUuid("user-1")).thenReturn(Optional.of(user));
        var view = new ScopeCatalogView(Map.of(), Map.of(), Set.of());
        var adapter = new IdentityLookupQueryAdapter(users, externalIdentities, entitlementMappings, assignments, () -> view);
        var context = new AccessContext("assignment-1", "unknown.scope", "resource-1");
        assertThatThrownBy(() -> adapter.resolveByPlatformUserId("local", "user-1", context))
                .isInstanceOf(com.identity.domain.exception.IdentityDomainValidationException.class);
    }

    @Test
    void authority_ineffectiveProviderMetadata_neverGrantsPermission() {
        var authority = new AuthorityEntity();
        authority.setActive(true);
        assertThat(authority.isEffective()).isFalse();
        authority.setOwnerKey("merchant");
        authority.setSourceRevision(2);
        assertThat(authority.isEffective()).isTrue();
        authority.setProviderLifecycle("RETIRED");
        assertThat(authority.isEffective()).isFalse();
        authority.setProviderLifecycle("ACTIVE");
        authority.setActive(false);
        assertThat(authority.isEffective()).isFalse();
    }

    private AccessAssignmentEntity assignment(
            String id,
            UserEntity user,
            String roleCode,
            String authorityCode,
            String scopeId
    ) {
        AuthorityEntity authority = new AuthorityEntity();
        authority.setUuid("authority-" + authorityCode);
        authority.setCode(authorityCode);
        authority.setCategory("identity");
        authority.setOwnerKey("identity");
        authority.setSourceRevision(1);
        authority.setName(authorityCode);
        authority.setActive(true);

        RoleEntity role = new RoleEntity();
        role.setUuid("role-" + roleCode);
        role.setCode(roleCode);
        role.setName(roleCode);
        role.setActive(true);
        role.setAuthorities(Set.of(authority));

        AccessAssignmentEntity assignment = new AccessAssignmentEntity();
        assignment.setUuid(id);
        assignment.setUser(user);
        assignment.setRole(role);
        assignment.setScopeKey("merchant.account");
        assignment.setScopeId(scopeId);
        assignment.setStatus(AccessAssignmentStatus.ACTIVE);
        assignment.setCreatedAt(Instant.now());
        assignment.setUpdatedAt(Instant.now());
        return assignment;
    }
}
