package com.grab.store.customer.internal.command.handler;

import com.customer.application.model.write.CustomerResult;
import com.customer.application.model.write.RegisterCustomerCommand;
import com.customer.application.port.inbound.RegisterCustomerUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.customer.internal.config.CustomerTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterCustomerCommandHandler implements CommandHandler<RegisterCustomerCommand, CustomerResult> {

    private final RegisterCustomerUseCase registerCustomerUseCase;

    @Override
    @CustomerTransactional
    public CustomerResult handle(RegisterCustomerCommand command) {
        return registerCustomerUseCase.execute(command);
    }

    @Override
    public Class<RegisterCustomerCommand> getCommandType() {
        return RegisterCustomerCommand.class;
    }
}
