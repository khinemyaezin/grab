package com.grab.store.customer.internal.event;

import com.customer.application.model.write.RegisterCustomerCommand;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.customer.internal.config.CustomerEnabled;
import com.grab.store.identity.events.UserRegisteredIntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@CustomerEnabled
@RequiredArgsConstructor
public class UserRegisteredIntegrationEventListener {
    private static final Logger log = Loggers.getLogger(UserRegisteredIntegrationEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    @EventListener
    public void onUserRegistered(UserRegisteredIntegrationEvent event) {
        log.info("Processing user registered event for userId={}", event.userId());
        commandBus.dispatch(new RegisterCustomerCommand(
                idGenerator.convertIdFrom(event.userId()),
                event.email(),
                event.email()
        ));
    }
}
