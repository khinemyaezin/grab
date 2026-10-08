package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.security.ScopeDeclaration;
import com.grab.store.shared.events.inventory.InventoryScopeManifestDeclaredIntegrationEvent;
import com.identity.application.model.write.RegisterScopeManifestCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityScopeManifestRegistrationListenerTest {
    @Test
    void dispatchesRegistrationCommandWithManifestDetails() {
        var commandBus = new CapturingCommandBus();
        var listener = new IdentityScopeManifestRegistrationListener(commandBus);
        var event = new InventoryScopeManifestDeclaredIntegrationEvent(
                1,
                List.of(new ScopeDeclaration("inventory.fulfillment-location", "merchant.account"))
        );

        listener.onScopeManifestDeclared(event);

        assertThat(commandBus.dispatched)
                .isEqualTo(new RegisterScopeManifestCommand("inventory", 1, event.scopes()));
    }

    private static final class CapturingCommandBus implements CommandBus {
        private Command<?> dispatched;

        @Override
        public <R> R dispatch(Command<R> command) {
            dispatched = command;
            return null;
        }
    }
}
