package com.grab.app.account.customer.internal.adapter.inbound.event;

import com.customer.application.model.write.RegisterCustomerCommand;
import com.grab.app.shared.events.identity.UserRegisteredIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class UserRegisteredIntegrationEventListener extends AbstractIntegrationEventListener {
    private static final Logger log = Loggers.getLogger(UserRegisteredIntegrationEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public UserRegisteredIntegrationEventListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            IdGenerator idGenerator,
            @Qualifier("customerTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("customerProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

    @EventListener
    public void onUserRegistered(UserRegisteredIntegrationEvent event) {
        processDelivery("customer.RegisterCustomer", () -> {
            log.info("Processing user registered event for userId={}", event.userId());
            Id userId = idGenerator.convertIdFrom(event.userId());
            RegisterCustomerCommand command = new RegisterCustomerCommand(
                    userId,
                    event.email(),
                    event.email()
            );
            commandBus.dispatch(command);
        });
    }
}
