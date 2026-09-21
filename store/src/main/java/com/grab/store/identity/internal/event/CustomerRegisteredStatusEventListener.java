package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.customer.events.CustomerRegisteredIntegrationEvent;
import com.grab.store.identity.internal.policy.CustomerRegistrationAccessPolicy;
import com.identity.application.model.write.ReplaceAccessCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerRegisteredStatusEventListener {
    private static final Logger log = Loggers.getLogger(CustomerRegisteredStatusEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final CustomerRegistrationAccessPolicy accessPolicy;

    @EventListener
    public void handleCustomerRegistered(CustomerRegisteredIntegrationEvent event) {
        log.info(
                "Placing customer account access for customerId={} and userId={}",
                event.customerId(),
                event.userId()
        );
        placeAccess(event.userId(), event.customerId());
    }

    private void placeAccess(String userIdValue, String customerIdValue) {
        CustomerRegistrationAccessPolicy.CustomerRegistrationContext context =
                new CustomerRegistrationAccessPolicy.CustomerRegistrationContext(customerIdValue);

        accessPolicy.placementsFor(context)
                .forEach(placement -> {
                    var userId = idGenerator.convertIdFrom(userIdValue);
                    var command = new ReplaceAccessCommand(
                            userId,
                            placement.platformCode(),
                            placement.placementCode(),
                            placement.scopeKey(),
                            placement.scopeId()
                    );
                    commandBus.dispatch(command);
                });
    }
}
