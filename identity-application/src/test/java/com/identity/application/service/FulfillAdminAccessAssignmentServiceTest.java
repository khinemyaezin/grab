package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.model.write.FulfillAdminAccessAssignmentCommand;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FulfillAdminAccessAssignmentServiceTest {
    private UserRepository users;
    private RoleRepository roles;
    private AccessAssignmentRepository assignments;
    private IdGenerator ids;
    private FulfillAdminAccessAssignmentService service;

    private final CommonId userId = new CommonId("usr-1");
    private final CommonId merchantId = new CommonId("mer-1");
    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        roles = mock(RoleRepository.class);
        assignments = mock(AccessAssignmentRepository.class);
        ids = mock(IdGenerator.class);
        when(ids.generateId()).thenReturn(new CommonId("assign-1"));
        service = new FulfillAdminAccessAssignmentService(users, roles, assignments, ids);
    }

    @Test
    void execute_whenRoleAssignable_shouldCreateAssignment() {
        User user = User.createLocal(userId, new Email("test@example.com"), new HashedPassword("hash12345"));
        when(users.findById(userId)).thenReturn(Optional.of(user));

        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM, true, true, Set.of()
        );
        when(roles.findByCode("MERCHANT_ADMIN")).thenReturn(Optional.of(role));
        when(assignments.findCurrentByUserAndScope(eq(userId), any())).thenReturn(List.of());
        when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FulfillAdminAccessAssignmentCommand command = new FulfillAdminAccessAssignmentCommand(
                "req-1", merchantId, userId, "MERCHANT_ADMIN", "merchant.account", 0, now
        );

        var result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo("usr-1");
        assertThat(result.roleCode()).isEqualTo("MERCHANT_ADMIN");
        verify(assignments).save(any(AccessAssignment.class));
    }

    @Test
    void execute_whenRoleNotAssignable_shouldThrowValidationException() {
        User user = User.createLocal(userId, new Email("test@example.com"), new HashedPassword("hash12345"));
        when(users.findById(userId)).thenReturn(Optional.of(user));

        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM, true, false, Set.of()
        );
        when(roles.findByCode("MERCHANT_ADMIN")).thenReturn(Optional.of(role));

        FulfillAdminAccessAssignmentCommand command = new FulfillAdminAccessAssignmentCommand(
                "req-1", merchantId, userId, "MERCHANT_ADMIN", "merchant.account", 0, now
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IdentityDomainValidationException.class)
                .hasMessageContaining("Role is not available for new assignments");
    }

    @Test
    void execute_whenUserNotFound_shouldThrowServiceException() {
        when(users.findById(userId)).thenReturn(Optional.empty());

        FulfillAdminAccessAssignmentCommand command = new FulfillAdminAccessAssignmentCommand(
                "req-1", merchantId, userId, "MERCHANT_ADMIN", "merchant.account", 0, now
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IdentityServiceException.class)
                .hasMessageContaining("User not found");
    }
}
