package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.security.AuthorityDefinition;
import com.grab.store.shared.events.identity.IdentityAuthorityManifestDeclaredIntegrationEvent;
import com.identity.application.model.write.RegisterAuthorityManifestCommand;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentityAuthorityManifestRegistrationListenerTest {

    @Test
    void dispatchesRegistrationCommandWithManifestDetails() {
        CapturingCommandBus commandBus = new CapturingCommandBus();
        IdentityAuthorityManifestRegistrationListener listener =
                new IdentityAuthorityManifestRegistrationListener(commandBus);
        var event = new IdentityAuthorityManifestDeclaredIntegrationEvent(
                3,
                List.of(new AuthorityDefinition("USER_READ", "User read", "Read users"))
        );

        listener.onAuthorityManifestDeclared(event);

        assertThat(commandBus.dispatched)
                .isEqualTo(new RegisterAuthorityManifestCommand("identity", 3, event.authorities()));
    }

    @Test
    void propagatesCommandDispatchFailures() {
        RuntimeException failure = new RuntimeException("registration failed");
        CommandBus commandBus = new CommandBus() {
            @Override
            public <R> R dispatch(Command<R> command) {
                throw failure;
            }
        };
        IdentityAuthorityManifestRegistrationListener listener =
                new IdentityAuthorityManifestRegistrationListener(commandBus);
        var event = new IdentityAuthorityManifestDeclaredIntegrationEvent(1, List.of());

        assertThatThrownBy(() -> listener.onAuthorityManifestDeclared(event))
                .isSameAs(failure);
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
