package com.grab.store.identity.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.identity.internal.command.RegisterCommand;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.User;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.policy.RegistrationAccessPolicy;
import com.identity.domain.policy.RegistrationAccessPolicyResolver;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterCommandHandlerTest {
    @Mock
    private UserRepository users;
    @Mock
    private PlatformRepository platforms;
    @Mock
    private AccessAssignmentRepository assignments;
    @Mock
    private PasswordHasher passwords;
    @Mock
    private IdGenerator ids;

    @Mock
    private RegistrationAccessPolicyResolver policyResolver;

    private RegistrationAccessPolicy policy;

    private RegisterCommandHandler handler;
    private RegisterCommand command;

    @BeforeEach
    void setUp() {
        policy = new RegistrationAccessPolicy() {
            @Override
            public String platformCode() { return "CUSTOMER_APP"; }
            @Override
            public AccessAssignment createAssignment(Id assignmentId, Id userId, Platform platform) {
                platform.requireSupportedRole("CUSTOMER");
                return AccessAssignment.create(assignmentId, userId, platform, "CUSTOMER", AccessScope.global(), null, null);
            }
        };
        handler = new RegisterCommandHandler(users, platforms, assignments, passwords, ids, policyResolver);
        command = new RegisterCommand("customer@example.com", "Password123!", "CUSTOMER_APP");
    }

    @Test
    void handle_withValidRegistration_shouldCreateUserAndGlobalCustomerAssignment() {
        when(users.findByEmail(new Email(command.email()))).thenReturn(Optional.empty());

        var platform = Optional.of(customerPlatform());
        when(platforms.findByCode("CUSTOMER_APP")).thenReturn(platform);
        when(policyResolver.resolve("CUSTOMER_APP")).thenReturn(policy);
        when(passwords.hash(command.password())).thenReturn(new HashedPassword("stored-hash"));
        when(ids.generateId()).thenReturn(new CommonId("user-1"), new CommonId("assignment-1"));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignments.save(any(AccessAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = handler.handle(command);

        ArgumentCaptor<AccessAssignment> assignment = ArgumentCaptor.forClass(AccessAssignment.class);
        verify(assignments).save(assignment.capture());
        assertThat(result.id()).isEqualTo("user-1");
        assertThat(assignment.getValue().getUserId().getValue()).isEqualTo("user-1");
        assertThat(assignment.getValue().getPlatformCode()).isEqualTo("CUSTOMER_APP");
        assertThat(assignment.getValue().getRoleCode()).isEqualTo("CUSTOMER");
        assertThat(assignment.getValue().getScope().isGlobal()).isTrue();
        assertThat(assignment.getValue().getAssignedBy()).isNull();
        assertThat(assignment.getValue().getExpiresAt()).isNull();
        InOrder persistenceOrder = inOrder(users, assignments);
        persistenceOrder.verify(users).save(any(User.class));
        persistenceOrder.verify(assignments).save(any(AccessAssignment.class));
    }

    @Test
    void handle_withDuplicateEmail_shouldCreateNeitherUserNorAssignment() {
        User existingUser = User.createLocal(new CommonId("existing-1"), new Email(command.email()), new HashedPassword("hash"));
        when(users.findByEmail(new Email(command.email()))).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IdentityServiceException.class);

        verify(users, never()).save(any());
        verify(assignments, never()).save(any());
    }

    @Test
    void handle_withMissingConfiguredPlatform_shouldCreateNeitherUserNorAssignment() {
        when(users.findByEmail(new Email(command.email()))).thenReturn(Optional.empty());
        when(platforms.findByCode("CUSTOMER_APP")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IdentityServiceException.class)
                .satisfies(exception -> assertThat(
                        ((IdentityServiceException) exception).getMessageSource().code()
                ).isEqualTo("idt.service.platform.not_found"));

        verify(users, never()).save(any());
        verify(assignments, never()).save(any());
    }

    @Test
    void handle_withUnsupportedConfiguredRole_shouldCreateNeitherUserNorAssignment() {
        when(users.findByEmail(new Email(command.email()))).thenReturn(Optional.empty());
        when(platforms.findByCode("CUSTOMER_APP")).thenReturn(Optional.of(new Platform(
                new CommonId("platform-1"),
                "CUSTOMER_APP",
                "Customer App",
                true,
                Set.of("MEMBER")
        )));
        prepareDomainConstruction();

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IdentityDomainValidationException.class);

        verify(users, never()).save(any());
        verify(assignments, never()).save(any());
    }

    @Test
    void handle_withInactiveConfiguredRole_shouldCreateNeitherUserNorAssignment() {
        when(users.findByEmail(new Email(command.email()))).thenReturn(Optional.empty());
        when(platforms.findByCode("CUSTOMER_APP")).thenReturn(Optional.of(new Platform(
                new CommonId("platform-1"),
                "CUSTOMER_APP",
                "Customer App",
                true,
                Set.of()
        )));
        prepareDomainConstruction();

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IdentityDomainValidationException.class);

        verify(users, never()).save(any());
        verify(assignments, never()).save(any());
    }

    private void prepareDomainConstruction() {
        when(passwords.hash(command.password())).thenReturn(new HashedPassword("stored-hash"));
        when(ids.generateId()).thenReturn(new CommonId("user-1"), new CommonId("assignment-1"));
        when(policyResolver.resolve("CUSTOMER_APP")).thenReturn(policy);
    }

    private Platform customerPlatform() {
        return new Platform(
                new CommonId("platform-1"),
                "CUSTOMER_APP",
                "Customer App",
                true,
                Set.of("CUSTOMER")
        );
    }
}
