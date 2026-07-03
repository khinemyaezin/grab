package com.grab.store.identity.internal.command.handler;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.identity.internal.command.GrantAccessCommand;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.service.RuleBasedRoleDelegationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrantAccessCommandHandlerTest {
    @Mock
    private UserRepository users;
    @Mock
    private PlatformRepository platforms;
    @Mock
    private RoleRepository roles;
    @Mock
    private AccessAssignmentRepository assignments;
    @Mock
    private IdGenerator ids;

    private GrantAccessCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GrantAccessCommandHandler(
                users,
                platforms,
                roles,
                assignments,
                new RuleBasedRoleDelegationPolicy(
                        (delegators, delegated) -> delegators.contains("MERCHANT_OWNER")
                                && delegated.equals("MERCHANT_ADMIN")
                ),
                ids
        );
    }

    @Test
    void handle_ownerDelegatesAdminInOwnMerchant_shouldGrantScopedAccess() {
        var command = command("target-user", "MERCHANT_ADMIN", "merchant-1", "assigner", Set.of("MERCHANT_OWNER"));
        when(users.findById(command.userId())).thenReturn(Optional.of(mock(User.class)));
        when(platforms.findByCode("SELLER_PORTAL")).thenReturn(Optional.of(sellerPlatform()));
        when(roles.findByCode(command.roleCode())).thenReturn(Optional.of(role(command.roleCode())));
        when(assignments.findCurrent(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(assignments.existsCurrent(any(), any(), any(), any())).thenReturn(false);
        when(ids.generateId()).thenReturn(new CommonId("assignment-1"));
        when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = handler.handle(command);

        assertThat(result.id()).isEqualTo("assignment-1");
        assertThat(result.roleCode()).isEqualTo("MERCHANT_ADMIN");
        assertThat(result.scopeId()).isEqualTo("merchant-1");
        verify(assignments).save(any(AccessAssignment.class));
    }

    @Test
    void handle_ownerAttemptsToGrantOwnerRole_shouldDenyDelegation() {
        var command = command("target-user", "MERCHANT_OWNER", "merchant-1", "assigner", Set.of("MERCHANT_OWNER"));
        when(users.findById(command.userId())).thenReturn(Optional.of(mock(User.class)));
        when(platforms.findByCode("SELLER_PORTAL")).thenReturn(Optional.of(sellerPlatform()));
        when(roles.findByCode(command.roleCode())).thenReturn(Optional.of(role(command.roleCode())));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IdentityDomainValidationException.class)
                .satisfies(exception -> assertThat(
                        ((IdentityDomainValidationException) exception).getMessageSource().code()
                ).isEqualTo("idt.domain.access.role_delegation_forbidden"));

        verify(assignments, never()).save(any());
    }

    @Test
    void handle_actorAttemptsToGrantOwnAccess_shouldDenySelfChange() {
        var command = command("same-user", "MERCHANT_ADMIN", "merchant-1", "same-user", Set.of("MERCHANT_OWNER"));
        when(users.findById(command.userId())).thenReturn(Optional.of(mock(User.class)));
        when(platforms.findByCode("SELLER_PORTAL")).thenReturn(Optional.of(sellerPlatform()));
        when(roles.findByCode(command.roleCode())).thenReturn(Optional.of(role(command.roleCode())));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IdentityDomainValidationException.class)
                .satisfies(exception -> assertThat(
                        ((IdentityDomainValidationException) exception).getMessageSource().code()
                ).isEqualTo("idt.domain.access.self_assignment_forbidden"));

        verify(assignments, never()).save(any());
    }

    private GrantAccessCommand command(
            String userId,
            String roleCode,
            String scopeId,
            String assignedBy,
            Set<String> actorRoles
    ) {
        return new GrantAccessCommand(
                new CommonId(userId),
                "SELLER_PORTAL",
                roleCode,
                "merchant.account",
                scopeId,
                null,
                new CommonId(assignedBy),
                "merchant.account",
                "merchant-1",
                actorRoles
        );
    }

    private Platform sellerPlatform() {
        return new Platform(
                new CommonId("seller-platform"),
                "SELLER_PORTAL",
                "Seller Portal",
                true,
                Set.of("MERCHANT_OWNER", "MERCHANT_ADMIN")
        );
    }

    private Role role(String roleCode) {
        return new Role(
                new CommonId("role-1"),
                roleCode,
                roleCode,
                null,
                true,
                Set.of("MERCHANT_PROFILE_READ")
        );
    }
}
