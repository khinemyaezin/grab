package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.PlatformRepository;
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

    private static final String PLATFORM_CODE = "SELLER_PORTAL";
    private static final String PREVIOUS_ROLE = "MERCHANT_STAFF";
    private static final String REPLACEMENT_ROLE = "MERCHANT_OWNER";
    private static final String ANOTHER_ROLE = "STORE_CASHIER";
    private static final String SCOPE_KEY = "merchant.account";
    private static final String SCOPE_ID = "store-123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformRepository platformRepository;

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
    private Platform platform;

    @BeforeEach
    void setUp() {
        service = new ReplaceAccessService(
                userRepository,
                platformRepository,
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

        platform = new Platform(
                () -> "plt-1",
                PLATFORM_CODE,
                "Seller Portal",
                true,
                Set.of(PREVIOUS_ROLE, REPLACEMENT_ROLE, ANOTHER_ROLE),
                Set.of(),
                Set.of()
        );
    }

    @Test
    void shouldCreateReplacementAccess_whenValidCommandAndNoPriorRole() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, PLATFORM_CODE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment previousAssignment = createAssignment(
                "asn-old-111", PREVIOUS_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment existingAssignment = createAssignment(
                "asn-existing-222", REPLACEMENT_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment suspended = createAssignment(
                "asn-suspended-333", REPLACEMENT_ROLE, AccessAssignmentStatus.SUSPENDED, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        Instant pastDate = Instant.parse("2020-01-01T00:00:00Z");
        AccessAssignment expired = createAssignment(
                "asn-expired-444", REPLACEMENT_ROLE, AccessAssignmentStatus.ACTIVE, pastDate
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, null, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment activeRole = createAssignment(
                "asn-to-revoke-555", REPLACEMENT_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, null, SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IdentityServiceException.class)
                .satisfies(ex -> {
                    IdentityServiceException isEx = (IdentityServiceException) ex;
                    assertThat(isEx.getMessageSource()).isInstanceOf(IdentityServiceError.UserNotFound.class);
                });

        verify(platformRepository, never()).findByCode(any());
        verify(assignmentRepository, never()).findCurrentByUserPlatformAndScope(any(), any(), any());
    }

    @Test
    void shouldThrowPlatformNotFound_whenPlatformDoesNotExist() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, "UNKNOWN_PORTAL", REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode("UNKNOWN_PORTAL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IdentityServiceException.class)
                .satisfies(ex -> {
                    IdentityServiceException isEx = (IdentityServiceException) ex;
                    assertThat(isEx.getMessageSource()).isInstanceOf(IdentityServiceError.PlatformNotFound.class);
                });

        verify(assignmentRepository, never()).findCurrentByUserPlatformAndScope(any(), any(), any());
    }

    @Test
    void shouldThrowException_whenRoleIsNotSupportedByPlatform() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, PLATFORM_CODE, "SUPER_ADMIN", SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IdentityDomainValidationException.class);

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void shouldRevokeOnlySpecifiedPreviousRole_andRetainOtherRolesInSameScope() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, PLATFORM_CODE, PREVIOUS_ROLE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment staffAssignment = createAssignment(
                "asn-staff-111", PREVIOUS_ROLE, AccessAssignmentStatus.ACTIVE, null
        );
        AccessAssignment cashierAssignment = createAssignment(
                "asn-cashier-222", ANOTHER_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, PREVIOUS_ROLE, null, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment staffAssignment = createAssignment(
                "asn-staff-111", PREVIOUS_ROLE, AccessAssignmentStatus.ACTIVE, null
        );
        AccessAssignment cashierAssignment = createAssignment(
                "asn-cashier-222", ANOTHER_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, PREVIOUS_ROLE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment staffAssignment = createAssignment(
                "asn-staff-111", PREVIOUS_ROLE, AccessAssignmentStatus.ACTIVE, null
        );
        AccessAssignment ownerAssignment = createAssignment(
                "asn-owner-333", REPLACEMENT_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
                userId, PLATFORM_CODE, PREVIOUS_ROLE, REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        AccessAssignment cashierAssignment = createAssignment(
                "asn-cashier-222", ANOTHER_ROLE, AccessAssignmentStatus.ACTIVE, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));
        when(assignmentRepository.findCurrentByUserPlatformAndScope(eq(userId), eq(PLATFORM_CODE), any(AccessScope.class)))
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
    void shouldThrowException_whenPreviousRoleIsNotSupportedByPlatform() {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId, PLATFORM_CODE, "UNKNOWN_ROLE", REPLACEMENT_ROLE, SCOPE_KEY, SCOPE_ID
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(platformRepository.findByCode(PLATFORM_CODE)).thenReturn(Optional.of(platform));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IdentityDomainValidationException.class);

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
                PLATFORM_CODE,
                roleCode,
                AccessScope.from(SCOPE_KEY, SCOPE_ID),
                status,
                () -> "admin-1",
                now,
                now,
                expiresAt
        );
    }
}
