package com.grab.app.account.customer.internal.command.handler;

import com.customer.application.model.write.AttachUserToCustomerCommand;
import com.customer.application.model.write.CustomerResult;
import com.customer.application.port.inbound.AttachUserToCustomerUseCase;
import com.grab.app.account.customer.internal.config.CustomerTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class AttachUserToCustomerCommandHandler implements CommandHandler<AttachUserToCustomerCommand, CustomerResult> {

    private final AttachUserToCustomerUseCase attachUserToCustomerUseCase;

    public AttachUserToCustomerCommandHandler(AttachUserToCustomerUseCase attachUserToCustomerUseCase) {
        this.attachUserToCustomerUseCase = attachUserToCustomerUseCase;
    }

    @Override
    @CustomerTransactional
    public CustomerResult handle(AttachUserToCustomerCommand command) {
        return attachUserToCustomerUseCase.execute(command);
    }

    @Override
    public Class<AttachUserToCustomerCommand> getCommandType() {
        return AttachUserToCustomerCommand.class;
    }
}

