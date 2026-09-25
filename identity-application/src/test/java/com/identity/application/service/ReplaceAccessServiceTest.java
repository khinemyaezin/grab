package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.model.Authority;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.SessionStore;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplaceAccessServiceTest {

    private static final String PREVIOUS_ROLE = "MERCHANT_STAFF";
    private static final String REPLACEMENT_ROLE = "MERCHANT_OWNER";
    private static final String ANOTHER_ROLE = "STORE_CASHIER";
    private static final String SCOPE_KEY = "merchant.account";
    private static final String SCOPE_ID = "store-123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private AccessAssignmentRepository assignmentRepository;

    @Mock
    private SessionStore sessionStore;

    @Mock
    private IdGenerator idGenerator;

    private ReplaceAccessService service;

    private Id userId;
    private Id newAssignmentId;
    private User activeUser;

    @BeforeEach
    void setUp() {
        service = new ReplaceAccessService(
                userRepository,
                assignmentRepository,
                sessionStore,
                idGenerator
        );

        userId = () -> "usr-100";
        newAssignmentId = () -> "asn-new-999";

        activeUser = new User(
                userId,
                new Email("seller@example.com"),
                new HashedPassword("$2a$10$validhashvalueforuser"),
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void shouldCreateReplacementAccess_whenValidCommandAndNoPriorRole() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of());
        when(idGenerator.generateId()).thenReturn(newAssignmentId);
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(command);

        assertThat(result.id()).isEqualTo("asn-new-999");
        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        assertThat(result.scopeKey()).isEqualTo(SCOPE_KEY);
        assertThat(result.scopeId()).isEqualTo(SCOPE_ID);
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());

        ArgumentCaptor<AccessAssignment> captor = ArgumentCaptor.forClass(AccessAssignment.class);
        verify(assignmentRepository).save(captor.capture());
        assertThat(captor.getValue().getRoleCode()).isEqualTo(REPLACEMENT_ROLE);
        verify(sessionStore, never()).revokeByAssignment(any());
    }

    @Test
    void shouldRevokePreviousAccessInScope_andIssueNewRole() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment previousAssignment = createAssignment(
                "asn-old-111", PREVIOUS_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(previousAssignment));
        when(idGenerator.generateId()).thenReturn(newAssignmentId);
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(command);

        assertThat(previousAssignment.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        verify(assignmentRepository).save(previousAssignment);
        verify(sessionStore).revokeByAssignment("asn-old-111");

        assertThat(result.id()).isEqualTo("asn-new-999");
        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());
    }

    @Test
    void shouldReturnExistingAssignment_whenUserAlreadyHasActiveRoleInScope() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment existingAssignment = createAssignment(
                "asn-existing-222", REPLACEMENT_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(existingAssignment));

        AccessAssignmentResult result = service.execute(command);

        assertThat(result.id()).isEqualTo("asn-existing-222");
        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());

        verify(idGenerator, never()).generateId();
        verify(assignmentRepository, never()).save(any());
        verify(sessionStore, never()).revokeByAssignment(any());
    }

    @Test
    void shouldRevokeSuspendedAssignment_andCreateActiveReplacement() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment suspended = createAssignment(
                "asn-suspended-333", REPLACEMENT_ROLE, AccessAssignmentStatus.SUSPENDED, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(suspended));
        when(idGenerator.generateId()).thenReturn(newAssignmentId);
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(command);

        assertThat(suspended.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        verify(sessionStore).revokeByAssignment("asn-suspended-333");
        assertThat(result.id()).isEqualTo("asn-new-999");
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());
    }

    @Test
    void shouldExpirePastDueAssignment_andCreateActiveReplacement() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        Instant pastDate = Instant.parse("2020-01-01T00:00:00Z");
        AccessAssignment expired = createAssignment(
                "asn-expired-444", REPLACEMENT_ROLE, AccessAssignmentStatus.ACTIVE, pastDate
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(expired));
        when(idGenerator.generateId()).thenReturn(newAssignmentId);
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(command);

        assertThat(expired.getStatus()).isEqualTo(AccessAssignmentStatus.EXPIRED);
        verify(sessionStore).revokeByAssignment("asn-expired-444");
        assertThat(result.id()).isEqualTo("asn-new-999");
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());
    }

    @Test
    void shouldPerformPureRevocation_whenReplacementRoleIsNull() {
        ReplaceAccessCommand revokeCommand = new ReplaceAccessCommand(
                userId, null, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment activeRole = createAssignment(
                "asn-to-revoke-555", REPLACEMENT_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(activeRole));
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(revokeCommand);

        assertThat(activeRole.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        verify(sessionStore).revokeByAssignment("asn-to-revoke-555");
        verify(idGenerator, never()).generateId();
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.REVOKED.name());
    }

    @Test
    void shouldReturnRevokedResult_whenReplacementRoleIsNullAndNoAssignmentExists() {
        ReplaceAccessCommand revokeCommand = new ReplaceAccessCommand(
                userId, null, SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of());

        AccessAssignmentResult result = service.execute(revokeCommand);

        assertThat(result.status()).isEqualTo("REVOKED");
        verify(assignmentRepository, never()).save(any());
        verify(sessionStore, never()).revokeByAssignment(any());
    }

    @Test
    @DisplayName("Should throw UserNotFound exception when user does not exist")
    void shouldThrowUserNotFound_whenUserDoesNotExist() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IdentityServiceException.class)
                .satisfies(ex -> {
                    IdentityServiceException isEx = (IdentityServiceException) ex;
                    assertThat(isEx.getMessageSource()).isInstanceOf(IdentityServiceError.UserNotFound.class);
                });

        verify(assignmentRepository, never()).findCurrentByUserAndScope(any(), any());
    }

    @Test
    void shouldRevokeOnlySpecifiedPreviousRole_andRetainOtherRolesInSameScope() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, PREVIOUS_ROLE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment staffAssignment = createAssignment(
                "asn-staff-111", PREVIOUS_ROLE, AccessAssignmentStatus.ACTIVE, null
        );
        AccessAssignment cashierAssignment = createAssignment(
                "asn-cashier-222", ANOTHER_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(staffAssignment, cashierAssignment));
        when(idGenerator.generateId()).thenReturn(newAssignmentId);
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(command);

        assertThat(staffAssignment.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        verify(assignmentRepository).save(staffAssignment);
        verify(sessionStore).revokeByAssignment("asn-staff-111");

        assertThat(cashierAssignment.getStatus()).isEqualTo(AccessAssignmentStatus.ACTIVE);
        verify(assignmentRepository, never()).save(cashierAssignment);
        verify(sessionStore, never()).revokeByAssignment("asn-cashier-222");

        assertThat(result.id()).isEqualTo("asn-new-999");
        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());
    }

    @Test
    void shouldRevokeOnlySpecifiedPreviousRole_whenReplacementRoleIsNull() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, PREVIOUS_ROLE, null, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment staffAssignment = createAssignment(
                "asn-staff-111", PREVIOUS_ROLE, AccessAssignmentStatus.ACTIVE, null
        );
        AccessAssignment cashierAssignment = createAssignment(
                "asn-cashier-222", ANOTHER_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(staffAssignment, cashierAssignment));
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(command);

        assertThat(staffAssignment.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        verify(assignmentRepository).save(staffAssignment);
        verify(sessionStore).revokeByAssignment("asn-staff-111");

        assertThat(cashierAssignment.getStatus()).isEqualTo(AccessAssignmentStatus.ACTIVE);
        verify(assignmentRepository, never()).save(cashierAssignment);
        verify(sessionStore, never()).revokeByAssignment("asn-cashier-222");

        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.REVOKED.name());
        assertThat(result.roleCode()).isEqualTo(PREVIOUS_ROLE);
    }

    @Test
    void shouldRetainExistingReplacementRole_andRevokeOnlyPreviousRole_whenBothPresent() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, PREVIOUS_ROLE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment staffAssignment = createAssignment(
                "asn-staff-111", PREVIOUS_ROLE, AccessAssignmentStatus.ACTIVE, null
        );
        AccessAssignment ownerAssignment = createAssignment(
                "asn-owner-333", REPLACEMENT_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(staffAssignment, ownerAssignment));
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(command);

        assertThat(staffAssignment.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        verify(assignmentRepository).save(staffAssignment);
        verify(sessionStore).revokeByAssignment("asn-staff-111");

        assertThat(ownerAssignment.getStatus()).isEqualTo(AccessAssignmentStatus.ACTIVE);
        verify(idGenerator, never()).generateId();

        assertThat(result.id()).isEqualTo("asn-owner-333");
        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());
    }

    @Test
    void shouldGrantReplacementRole_whenPreviousRoleIsNotHeldByUser() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, PREVIOUS_ROLE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment cashierAssignment = createAssignment(
                "asn-cashier-222", ANOTHER_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class)))
                .thenReturn(List.of(cashierAssignment));
        when(idGenerator.generateId()).thenReturn(newAssignmentId);
        when(assignmentRepository.save(any(AccessAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = service.execute(command);

        assertThat(cashierAssignment.getStatus()).isEqualTo(AccessAssignmentStatus.ACTIVE);
        verify(sessionStore, never()).revokeByAssignment("asn-cashier-222");

        assertThat(result.id()).isEqualTo("asn-new-999");
        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());
    }

    @Test
    void shouldSkipRoleCreation_whenRoleAlreadyExists() {
        ReplaceAccessService serviceWithRoles = new ReplaceAccessService(
                userRepository,
                roleRepository,
                authorityRepository,
                assignmentRepository,
                sessionStore,
                idGenerator
        );

        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, null, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID, Set.of("USER_READ")
        );

        Role existingRole = mock(Role.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(roleRepository.findByCode(REPLACEMENT_ROLE)).thenReturn(Optional.of(existingRole));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class))).thenReturn(List.of());
        when(idGenerator.generateId()).thenReturn(newAssignmentId);
        when(assignmentRepository.save(any(AccessAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = serviceWithRoles.execute(command);

        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        verify(roleRepository, never()).save(any());
        verify(authorityRepository, never()).findActiveByCodes(any());
    }

    @Test
    void shouldCreateRoleWithActiveAuthorities_whenRoleDoesNotExist() {
        ReplaceAccessService serviceWithRoles = new ReplaceAccessService(
                userRepository,
                roleRepository,
                authorityRepository,
                assignmentRepository,
                sessionStore,
                idGenerator
        );

        Set<String> requestedAuths = Set.of("USER_READ", "INVALID_CODE!#", "*");
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, null, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID, requestedAuths
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(roleRepository.findByCode(REPLACEMENT_ROLE)).thenReturn(Optional.empty());
        when(authorityRepository.findActiveByCodes(Set.of("USER_READ")))
                .thenReturn(Set.of(authority("USER_READ")));
        when(assignmentRepository.findCurrentByUserAndScope(eq(userId), any(AccessScope.class))).thenReturn(List.of());
        when(idGenerator.generateId()).thenReturn(() -> "role-new-123", newAssignmentId);
        when(assignmentRepository.save(any(AccessAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessAssignmentResult result = serviceWithRoles.execute(command);

        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        Role savedRole = roleCaptor.getValue();
        assertThat(savedRole.getCode()).isEqualTo(REPLACEMENT_ROLE);
        assertThat(savedRole.getAuthorities().stream().map(Authority::getCode))
                .containsExactly("USER_READ");
    }

    @Test
    void shouldThrowRoleNotFound_whenRoleDoesNotExistAndNoActiveAuthoritiesMatch() {
        ReplaceAccessService serviceWithRoles = new ReplaceAccessService(
                userRepository,
                roleRepository,
                authorityRepository,
                assignmentRepository,
                sessionStore,
                idGenerator
        );

        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, null, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID, Set.of("UNKNOWN_AUTH")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(roleRepository.findByCode(REPLACEMENT_ROLE)).thenReturn(Optional.empty());
        when(authorityRepository.findActiveByCodes(Set.of("UNKNOWN_AUTH"))).thenReturn(Set.of());

        assertThatThrownBy(() -> serviceWithRoles.execute(command))
                .isInstanceOf(IdentityServiceException.class)
                .satisfies(ex -> {
                    IdentityServiceException isEx = (IdentityServiceException) ex;
                    assertThat(isEx.getMessageSource()).isInstanceOf(IdentityServiceError.RoleNotFound.class);
                });

        verify(roleRepository, never()).save(any());
        verify(assignmentRepository, never()).save(any());
    }

    private AccessAssignment createAssignment(
            String id,
            String roleCode,
            AccessAssignmentStatus status,
            Instant expiresAt
    ) {
        Instant now = Instant.parse("2026-07-02T00:00:00Z");
        return new AccessAssignment(
                () -> id,
                userId,
                roleCode,
                AccessScope.from(SCOPE_KEY, SCOPE_ID),
                status,
                () -> "admin-1",
                now,
                now,
                expiresAt
        );
    }

    private Authority authority(String code) {
        return Authority.rehydrate(new CommonId("authority-" + code), code, "identity", code, null, true);
    }
}
