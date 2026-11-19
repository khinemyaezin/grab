package com.grab.service.authorizationserver.core.user;

import com.grab.service.authorizationserver.port.outbound.ScimClientPort;
import com.grab.service.contract.message.account.scim.ScimAccessContext;
import com.grab.service.contract.message.account.scim.ScimAccessContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContextSelectionServiceTest {
    private ScimClientPort scimClient;
    private IamAccessContextRepository contextRepo;
    private IamUserRepository userRepo;
    private ContextSelectionService service;

    @BeforeEach
    void setUp() {
        scimClient = mock(ScimClientPort.class);
        contextRepo = mock(IamAccessContextRepository.class);
        userRepo = mock(IamUserRepository.class);
        service = new ContextSelectionService(userRepo, contextRepo, scimClient);
    }

    @Test
    void syncsContextsFromScimAndPersistsLocally() {
        String iamUserId = "iam-user-1";
        IamUser user = new IamUser(iamUserId, "seller", "seller@example.com", "hash", "John", "Doe",
                true, "platform-user-1", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepo.findById(iamUserId)).thenReturn(Optional.of(user));

        ScimAccessContext scimCtx = new ScimAccessContext(
                "assign-1", "merchant.account", "merchant-123", "MERCHANT_OWNER", "Acme Store",
                Set.of("catalog:read", "catalog:write")
        );
        ScimAccessContextExtension extension = new ScimAccessContextExtension(List.of(scimCtx));
        when(scimClient.getUserAccessContexts("platform-user-1")).thenReturn(extension);

        List<IamAccessContext> result = service.getOrSyncContexts(iamUserId);

        assertThat(result).hasSize(1);
        IamAccessContext mapped = result.getFirst();
        assertThat(mapped.assignmentId()).isEqualTo("assign-1");
        assertThat(mapped.scopeKey()).isEqualTo("merchant.account");
        assertThat(mapped.scopeId()).isEqualTo("merchant-123");
        assertThat(mapped.displayName()).isEqualTo("Acme Store");
        assertThat(mapped.role()).isEqualTo("MERCHANT_OWNER");
        assertThat(mapped.authorities()).containsExactlyInAnyOrder("catalog:read", "catalog:write");

        verify(contextRepo).replaceUserContexts(eq(iamUserId), eq(result));
    }

    @Test
    void fallsBackToLocalContextsWhenScimCallFails() {
        String iamUserId = "iam-user-2";
        IamUser user = new IamUser(iamUserId, "fallback", "fallback@example.com", "hash", "Fall", "Back",
                true, "platform-user-2", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepo.findById(iamUserId)).thenReturn(Optional.of(user));
        when(scimClient.getUserAccessContexts("platform-user-2")).thenThrow(new RuntimeException("RMI unavailable"));

        IamAccessContext cached = new IamAccessContext(
                "ctx-1", iamUserId, "assign-cached", "merchant.storefront", "store-999",
                "STORE_MANAGER", "Downtown Branch", Set.of("inventory:manage"), Instant.now()
        );
        when(contextRepo.findByUserId(iamUserId)).thenReturn(List.of(cached));

        List<IamAccessContext> result = service.getOrSyncContexts(iamUserId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().assignmentId()).isEqualTo("assign-cached");
    }

    @Test
    void resolvesSelectedContextForValidAssignment() {
        String iamUserId = "iam-user-3";
        IamUser user = new IamUser(iamUserId, "seller", "seller@example.com", "hash", "Jane", "Doe",
                true, "platform-3", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepo.findById(iamUserId)).thenReturn(Optional.of(user));

        ScimAccessContext scimCtx = new ScimAccessContext(
                "assign-10", "merchant.account", "merchant-456", "MERCHANT_OWNER", "Super Mart",
                Set.of("orders:view")
        );
        when(scimClient.getUserAccessContexts("platform-3")).thenReturn(new ScimAccessContextExtension(List.of(scimCtx)));

        SelectedContext selected = service.resolveSelection(iamUserId, "assign-10");

        assertThat(selected.assignmentId()).isEqualTo("assign-10");
        assertThat(selected.platformUserId()).isEqualTo("platform-3");
        assertThat(selected.scopeKey()).isEqualTo("merchant.account");
        assertThat(selected.scopeId()).isEqualTo("merchant-456");
        assertThat(selected.role()).isEqualTo("MERCHANT_OWNER");
        assertThat(selected.authorities()).containsExactly("orders:view");
        assertThat(selected.onboarding()).isFalse();
    }

    @Test
    void throwsExceptionWhenSelectedAssignmentIsNotFound() {
        String iamUserId = "iam-user-4";
        IamUser user = new IamUser(iamUserId, "seller", "seller@example.com", "hash", "Jane", "Doe",
                true, "platform-4", 1L, Set.of(), Instant.now(), Instant.now());
        when(userRepo.findById(iamUserId)).thenReturn(Optional.of(user));
        when(scimClient.getUserAccessContexts("platform-4")).thenReturn(new ScimAccessContextExtension(List.of()));
        when(contextRepo.findByUserId(iamUserId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.resolveSelection(iamUserId, "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid assignment ID: unknown");
    }
}
