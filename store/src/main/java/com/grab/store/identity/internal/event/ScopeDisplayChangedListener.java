package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.shared.events.identity.ScopeDisplayChangedIntegrationEvent;
import com.identity.application.model.write.UpdateScopeDisplayCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScopeDisplayChangedListener {
    private final CommandBus commandBus;
    private final IdGenerator ids;

    @EventListener
    public void onScopeDisplayChanged(ScopeDisplayChangedIntegrationEvent event) {
        Id scopeId = ids.convertIdFrom(event.scopeId());
        String name = event.name();
        UpdateScopeDisplayCommand command = new UpdateScopeDisplayCommand(scopeId, name);
        commandBus.dispatch(command);
    }
}
