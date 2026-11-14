package com.grab.app.account.customer.internal.adapter.inbound.event;

import com.grab.framework.id.Id;
import java.util.UUID;
import com.grab.framework.outbox.OutboxDeliveryContext;
import com.customer.application.model.write.RegisterCustomerCommand;
import com.grab.app.shared.events.identity.UserRegisteredIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredIntegrationEventListener {
    private static final Logger log = Loggers.getLogger(UserRegisteredIntegrationEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public UserRegisteredIntegrationEventListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            IdGenerator idGenerator
    ) {
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

    @EventListener
    public void onUserRegistered(UserRegisteredIntegrationEvent event) {
        log.info("Processing user registered event for userId={}", event.userId());
        Id userId = idGenerator.convertIdFrom(event.userId());
        UUID sourceEventId = OutboxDeliveryContext.currentEventId().orElse(null);
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                userId,
                event.email(),
                event.email(),
                sourceEventId
        );
        commandBus.dispatch(command);
    }
}
