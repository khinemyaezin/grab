package com.grab.app.account.customer.internal.command.handler;

import com.customer.application.model.write.CustomerResult;
import com.customer.application.model.write.RegisterCustomerCommand;
import com.customer.application.port.inbound.RegisterCustomerUseCase;
import com.grab.app.account.customer.internal.config.CustomerTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RegisterCustomerCommandHandler implements CommandHandler<RegisterCustomerCommand, CustomerResult> {

    private final RegisterCustomerUseCase registerCustomerUseCase;

    public RegisterCustomerCommandHandler(RegisterCustomerUseCase registerCustomerUseCase) {
        this.registerCustomerUseCase = registerCustomerUseCase;
    }

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

