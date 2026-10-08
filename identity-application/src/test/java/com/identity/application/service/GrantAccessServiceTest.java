package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.model.write.GrantAccessCommand;
import com.identity.application.port.outbound.ScopeOwnershipPort;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.aggregate.User;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.security.CatalogScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrantAccessServiceTest {
    @Mock UserRepository users;
    @Mock RoleRepository roles;
    @Mock AccessAssignmentRepository assignments;
    @Mock RoleDelegationPolicy delegation;
    @Mock IdGenerator ids;
    @Mock ScopeOwnershipPort ownership;
    @Mock SecurityCatalogRepository catalogs;
    private GrantAccessService service;
    private GrantAccessCommand command;

    @BeforeEach
    void setUp() {
        var userId = new CommonId("user");
        command = new GrantAccessCommand(userId, "MANAGER", "inventory.location", "location-1", null,
                new CommonId("actor"), "merchant.account", "merchant-1", Set.of("OWNER"));
        service = new GrantAccessService(users, roles, assignments, delegation, ids, ownership, catalogs);
        when(users.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(roles.findByCode("MANAGER")).thenReturn(Optional.of(mock(Role.class)));
        when(catalogs.loadForUpdate()).thenReturn(catalog(Lifecycle.ACTIVE));
    }

    @Test
    void grant_typeAncestryDoesNotReplaceResourceOwnership() {
        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(IdentityServiceException.class);
        verify(ownership).isResourceOwnedByScope("merchant.account", "merchant-1", "inventory.location", "location-1");
        verify(assignments, never()).save(any());
    }

    @Test
    void grant_approvedOwnerCapability_allowsOwnedResource() {
        when(ownership.isResourceOwnedByScope("merchant.account", "merchant-1", "inventory.location", "location-1")).thenReturn(true);
        when(ids.generateId()).thenReturn(new CommonId("assignment"));
        when(assignments.save(any(AccessAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.execute(command);
        assertThat(result.scopeId()).isEqualTo("location-1");
        verify(delegation).requireCanDelegate(Set.of("OWNER"), "MANAGER");
    }

    @Test
    void grant_retiredAncestor_deniesBeforeResourceCapability() {
        when(catalogs.loadForUpdate()).thenReturn(catalog(Lifecycle.RETIRED));
        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(IdentityDomainValidationException.class);
        verifyNoInteractions(ownership);
        verify(assignments, never()).save(any());
    }

    private SecurityCatalog catalog(Lifecycle parent) {
        var root = new CatalogScope("merchant.account", "merchant", null, parent, true, 2);
        var child = new CatalogScope("inventory.location", "inventory", "merchant.account", Lifecycle.ACTIVE, true, 2);
        return SecurityCatalog.rehydrate(new CommonId("catalog"), 1, List.of(root, child), List.of(), List.of());
    }
}
