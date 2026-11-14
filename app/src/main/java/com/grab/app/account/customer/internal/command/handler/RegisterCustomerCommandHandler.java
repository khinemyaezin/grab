package com.grab.app.account.customer.internal.command.handler;

import com.customer.application.model.write.CustomerResult;
import com.customer.application.model.write.RegisterCustomerCommand;
import com.customer.application.port.inbound.RegisterCustomerUseCase;
import com.grab.app.account.customer.internal.config.CustomerTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.inbox.ProcessedEventRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegisterCustomerCommandHandler implements CommandHandler<RegisterCustomerCommand, CustomerResult> {

    static final String CONSUMER = "customer.RegisterCustomer";

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final ProcessedEventRepository processedEvents;

    public RegisterCustomerCommandHandler(
            RegisterCustomerUseCase registerCustomerUseCase,
            @Qualifier("customerProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.processedEvents = processedEvents;
    }

    /**
     * Returns {@code null} when the triggering outbox event was already applied (duplicate delivery).
     */
    @Override
    @CustomerTransactional
    public CustomerResult handle(RegisterCustomerCommand command) {
        UUID sourceEventId = command.sourceEventId();
        if (sourceEventId != null && !processedEvents.markProcessed(CONSUMER, sourceEventId)) {
            return null;
        }
        return registerCustomerUseCase.execute(command);
    }

    @Override
    public Class<RegisterCustomerCommand> getCommandType() {
        return RegisterCustomerCommand.class;
    }
}
