package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RoleDeclarationDeclaredIntegrationEvent;
import com.grab.framework.security.role.RolePermissionReference;
import com.grab.store.shared.events.merchant.MerchantRoleDeclarationDeclaredIntegrationEvent;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentityRoleDeclarationRegistrationListenerTest {
    private static final class RecordingCommandBus implements CommandBus {
        private Command<?> dispatched;

        @Override
        public <R> R dispatch(Command<R> command) {
            dispatched = command;
            return null;
        }
    }

    @Test
    void onRoleDeclarationDeclaredDispatchesRegistrationCommand() {
        RecordingCommandBus commands = new RecordingCommandBus();
        IdentityRoleDeclarationRegistrationListener listener =
                new IdentityRoleDeclarationRegistrationListener(commands);
        RoleDeclaration declaration = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1, Map.of("merchant", 1),
                List.of(new RolePermissionReference("merchant", "MERCHANT_PROFILE_READ")));
        MerchantRoleDeclarationDeclaredIntegrationEvent event =
                new MerchantRoleDeclarationDeclaredIntegrationEvent(declaration, "event-1");

        listener.onRoleDeclarationDeclared(event);

        RegisterRoleDeclarationCommand command =
                assertInstanceOf(RegisterRoleDeclarationCommand.class, commands.dispatched);
        assertEquals(declaration, command.declaration());
        assertEquals("event-1", command.eventId());
        assertEquals(declaration.contentDigest(), command.suppliedContentDigest());
    }

    @Test
    void onRoleDeclarationDeclaredRejectsUnauthorizedOwner() {
        RecordingCommandBus commands = new RecordingCommandBus();
        IdentityRoleDeclarationRegistrationListener listener =
                new IdentityRoleDeclarationRegistrationListener(commands);
        RoleDeclaration declaration = new RoleDeclaration(
                "catalog", "CATALOG_ADMIN", "catalog.root", 1, Map.of("catalog", 1),
                List.of(new RolePermissionReference("catalog", "CATALOG_READ")));
        RoleDeclarationDeclaredIntegrationEvent event =
                new MerchantRoleDeclarationDeclaredIntegrationEvent(declaration, "event-1");

        assertThrows(IllegalArgumentException.class, () -> listener.onRoleDeclarationDeclared(event));
        assertNull(commands.dispatched);
    }
}
