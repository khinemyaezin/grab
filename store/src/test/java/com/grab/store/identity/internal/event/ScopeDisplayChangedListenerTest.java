package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.shared.events.identity.ScopeDisplayChangedIntegrationEvent;
import com.identity.application.model.write.UpdateScopeDisplayCommand;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeDisplayChangedListenerTest {
    private final List<Command<?>> dispatched = new ArrayList<>();
    private final CommandBus commandBus = new CommandBus() {
        @Override
        public <R> R dispatch(Command<R> command) {
            dispatched.add(command);
            return null;
        }
    };
    private final IdGenerator ids = new IdGenerator() {
        @Override
        public com.grab.framework.id.Id generateId() {
            return new CommonId("generated");
        }

        @Override
        public com.grab.framework.id.Id convertIdFrom(String value) {
            return new CommonId(value);
        }
    };
    private final ScopeDisplayChangedListener listener = new ScopeDisplayChangedListener(commandBus, ids);

    @Test
    void onScopeDisplayChanged_dispatchesUpdateScopeDisplayCommand() {
        ScopeDisplayChangedIntegrationEvent event = new ScopeDisplayChangedIntegrationEvent(
                "merchant-123", "Acme Store");

        listener.onScopeDisplayChanged(event);

        assertThat(dispatched).containsExactly(
                new UpdateScopeDisplayCommand(new CommonId("merchant-123"), "Acme Store"));
    }
}
