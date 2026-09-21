package com.grab.store.customer.internal.command.handler;

import com.customer.application.model.write.CreateGuestCustomerCommand;
import com.customer.application.model.write.CustomerResult;
import com.customer.application.port.inbound.CreateGuestCustomerUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.customer.internal.config.CustomerTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateGuestCustomerCommandHandler implements CommandHandler<CreateGuestCustomerCommand, CustomerResult> {

    private final CreateGuestCustomerUseCase createGuestCustomerUseCase;

    @Override
    @CustomerTransactional
    public CustomerResult handle(CreateGuestCustomerCommand command) {
        return createGuestCustomerUseCase.execute(command);
    }

    @Override
    public Class<CreateGuestCustomerCommand> getCommandType() {
        return CreateGuestCustomerCommand.class;
    }
}
