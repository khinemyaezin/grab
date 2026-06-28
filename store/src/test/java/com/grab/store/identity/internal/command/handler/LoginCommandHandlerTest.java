package com.grab.store.identity.internal.command.handler;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.store.identity.internal.command.LoginCommand;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.shared.security.LocalJwtProperties;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.AccessScopeType;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.service.TokenLifeCycle;
import com.identity.domain.service.TokenPair;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginCommandHandlerTest {
    @Mock
    private UserRepository users;
    @Mock
    private AccessAssignmentRepository assignments;
    @Mock
    private PasswordHasher passwords;
    @Mock
    private TokenLifeCycle tokens;
    @Mock
    private PlatformIdentityResolver identities;
    @Mock
    private LocalJwtProperties jwtProperties;

    private LoginCommandHandler handler;
    private User user;

    @BeforeEach
    void setUp() {
        handler = new LoginCommandHandler(users, assignments, passwords, tokens, identities, jwtProperties);
        user = User.createLocal(
                new CommonId("user-1"),
                new Email("customer@example.com"),
                new HashedPassword("stored-hash"),
                "CUSTOMER"
        );
        when(users.findByEmail(new Email("customer@example.com"))).thenReturn(Optional.of(user));
        when(passwords.verify("Password123!", user.getPasswordHash().orElseThrow())).thenReturn(true);
    }

    @Test
    void handle_withOnePlatformAssignment_shouldIssueContextBoundSession() {
        AccessAssignment assignment = assignment("assignment-1", "merchant-1");
        when(assignments.findEffectiveByUserAndPlatform(
                org.mockito.ArgumentMatchers.eq(user.getId()),
                org.mockito.ArgumentMatchers.eq("SELLER_PORTAL"),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(List.of(assignment));
        when(jwtProperties.issuer()).thenReturn("local-issuer");
        when(identities.resolve(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            ExternalPrincipal principal = invocation.getArgument(0);
            return new AuthenticatedActor(
                    "user-1", "local-issuer", "user-1", "customer@example.com",
                    Set.of("MERCHANT_OWNER"), Set.of("MERCHANT_WRITE_OWN"), principal.accessContext()
            );
        });
        when(tokens.issue(org.mockito.ArgumentMatchers.any())).thenReturn(new TokenPair("access", "refresh", 60000));

        var result = handler.handle(new LoginCommand(
                "customer@example.com", "Password123!", "SELLER_PORTAL", null
        ));

        ArgumentCaptor<ExternalPrincipal> principal = ArgumentCaptor.forClass(ExternalPrincipal.class);
        verify(identities).resolve(principal.capture());
        assertThat(principal.getValue().accessContext()).isPresent().hasValueSatisfying(context -> {
            assertThat(context.platformCode()).isEqualTo("SELLER_PORTAL");
            assertThat(context.assignmentId()).isEqualTo("assignment-1");
            assertThat(context.scopeId()).isEqualTo("merchant-1");
        });
        assertThat(result.accessToken()).isEqualTo("access");
    }

    @Test
    void handle_withSeveralPlatformAssignments_shouldRequireExplicitSelection() {
        when(assignments.findEffectiveByUserAndPlatform(
                org.mockito.ArgumentMatchers.eq(user.getId()),
                org.mockito.ArgumentMatchers.eq("SELLER_PORTAL"),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(List.of(
                assignment("assignment-1", "merchant-1"),
                assignment("assignment-2", "merchant-2")
        ));

        assertThatThrownBy(() -> handler.handle(new LoginCommand(
                "customer@example.com", "Password123!", "SELLER_PORTAL", null
        )))
                .isInstanceOf(IdentityServiceException.class)
                .satisfies(exception -> assertThat(
                        ((IdentityServiceException) exception).getMessageSource().code()
                ).isEqualTo("idt.service.access.context_selection_required"));

        verify(identities, never()).resolve(org.mockito.ArgumentMatchers.any());
        verify(tokens, never()).issue(org.mockito.ArgumentMatchers.any());
    }

    private AccessAssignment assignment(String id, String merchantId) {
        return AccessAssignment.create(
                new CommonId(id),
                user.getId(),
                new Platform(
                        new CommonId("seller-platform"),
                        "SELLER_PORTAL",
                        "Seller Portal",
                        true,
                        Set.of("MERCHANT_OWNER")
                ),
                "MERCHANT_OWNER",
                new AccessScope(AccessScopeType.MERCHANT_ACCOUNT, merchantId),
                new CommonId("admin-1"),
                null
        );
    }
}
