package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.impl.CommonId;
import com.grab.service.contract.message.account.scim.ScimAccessContextExtension;
import com.grab.service.contract.message.account.scim.ScimEmail;
import com.grab.service.contract.message.account.scim.ScimName;
import com.grab.service.contract.message.account.scim.ScimUser;
import com.identity.application.model.read.ExternalUserView;
import com.identity.application.model.read.GetExternalUserQuery;
import com.identity.application.model.read.GetUserAccessContextsQuery;
import com.identity.application.model.read.UserAccessContextsResult;
import com.identity.application.model.read.UserAccessContextsResult.AccessContextView;
import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.ProvisionExternalUserCommand;
import com.identity.application.model.write.UpdateExternalUserStatusCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScimUserPortAdapterTest {
    private CommandBus commandBus;
    private QueryBus queryBus;
    private ScimUserPortAdapter portAdapter;

    @BeforeEach
    void setUp() {
        commandBus = mock(CommandBus.class);
        queryBus = mock(QueryBus.class);
        ScimUserReadAdapter readAdapter = new ScimUserReadAdapter(queryBus);
        ScimUserWriteAdapter writeAdapter = new ScimUserWriteAdapter(commandBus);
        portAdapter = new ScimUserPortAdapter(readAdapter, writeAdapter);
    }

    @Test
    void provisionUser_dispatchesCommandAndReturnsScimUser() throws RemoteException {
        ExternalUserProvisionResult result = new ExternalUserProvisionResult(
                new CommonId("user-100"),
                "iam-sub-42",
                "seller@example.com",
                "John",
                "Doe",
                "ACTIVE",
                true
        );
        when(commandBus.dispatch(any(ProvisionExternalUserCommand.class))).thenReturn(result);

        ScimUser request = new ScimUser(
                "iam-sub-42",
                "seller@example.com",
                new ScimName("John", "Doe"),
                List.of(new ScimEmail("seller@example.com", "work", true)),
                true
        );

        ScimUser provisioned = portAdapter.provisionUser(request);

        assertThat(provisioned.id()).isEqualTo("user-100");
        assertThat(provisioned.externalId()).isEqualTo("iam-sub-42");
        assertThat(provisioned.userName()).isEqualTo("seller@example.com");
        assertThat(provisioned.active()).isTrue();
        assertThat(provisioned.schemas()).contains(ScimUser.SCHEMA_URI);

        verify(commandBus).dispatch(any(ProvisionExternalUserCommand.class));
    }

    @Test
    void getUser_resolvesUserAndAccessContexts() throws RemoteException {
        ExternalUserView userView = new ExternalUserView(
                new CommonId("user-100"),
                "iam-sub-42",
                "seller@example.com",
                "ACTIVE",
                1L
        );
        when(queryBus.dispatch(any(GetExternalUserQuery.class))).thenReturn(userView);

        AccessContextView ctx = new AccessContextView(
                "assign-1", "merchant.account", "merchant-123", "MERCHANT_OWNER",
                "Acme Corp", Set.of("ORDER_READ", "ORDER_WRITE")
        );
        UserAccessContextsResult contextsResult = new UserAccessContextsResult(
                new CommonId("user-100"), List.of(ctx)
        );
        when(queryBus.dispatch(any(GetUserAccessContextsQuery.class))).thenReturn(contextsResult);

        ScimUser user = portAdapter.getUser("iam-sub-42");

        assertThat(user).isNotNull();
        assertThat(user.id()).isEqualTo("user-100");
        assertThat(user.externalId()).isEqualTo("iam-sub-42");
        assertThat(user.accessContexts()).isNotNull();
        assertThat(user.accessContexts().contexts()).hasSize(1);

        var firstContext = user.accessContexts().contexts().getFirst();
        assertThat(firstContext.assignmentId()).isEqualTo("assign-1");
        assertThat(firstContext.scopeKey()).isEqualTo("merchant.account");
        assertThat(firstContext.scopeId()).isEqualTo("merchant-123");
        assertThat(firstContext.role()).isEqualTo("MERCHANT_OWNER");
        assertThat(firstContext.displayName()).isEqualTo("Acme Corp");
        assertThat(firstContext.authorities()).containsExactlyInAnyOrder("ORDER_READ", "ORDER_WRITE");
    }

    @Test
    void updateUserStatus_dispatchesUpdateCommand() throws RemoteException {
        ExternalUserProvisionResult result = new ExternalUserProvisionResult(
                new CommonId("user-100"),
                "iam-sub-42",
                "seller@example.com",
                null,
                null,
                "SUSPENDED",
                false
        );
        when(commandBus.dispatch(any(UpdateExternalUserStatusCommand.class))).thenReturn(result);

        ScimUser updated = portAdapter.updateUserStatus("iam-sub-42", false);

        assertThat(updated.id()).isEqualTo("user-100");
        assertThat(updated.active()).isFalse();

        verify(commandBus).dispatch(any(UpdateExternalUserStatusCommand.class));
    }

    @Test
    void getUserAccessContexts_returnsAccessContextExtension() throws RemoteException {
        ExternalUserView userView = new ExternalUserView(
                new CommonId("user-100"),
                "iam-sub-42",
                "seller@example.com",
                "ACTIVE",
                1L
        );
        when(queryBus.dispatch(any(GetExternalUserQuery.class))).thenReturn(userView);

        AccessContextView ctx = new AccessContextView(
                "assign-2", "merchant.storefront", "store-456", "STORE_MANAGER",
                "Downtown Store", Set.of("CATALOG_READ")
        );
        UserAccessContextsResult contextsResult = new UserAccessContextsResult(
                new CommonId("user-100"), List.of(ctx)
        );
        when(queryBus.dispatch(any(GetUserAccessContextsQuery.class))).thenReturn(contextsResult);

        ScimAccessContextExtension extension = portAdapter.getUserAccessContexts("iam-sub-42");

        assertThat(extension.contexts()).hasSize(1);
        assertThat(extension.contexts().getFirst().scopeKey()).isEqualTo("merchant.storefront");
        assertThat(extension.contexts().getFirst().scopeId()).isEqualTo("store-456");
        assertThat(extension.contexts().getFirst().role()).isEqualTo("STORE_MANAGER");
    }
}
