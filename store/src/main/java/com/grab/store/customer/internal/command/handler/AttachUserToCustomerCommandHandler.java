package com.grab.store.customer.internal.command.handler;

import com.customer.application.model.write.AttachUserToCustomerCommand;
import com.customer.application.model.write.CustomerResult;
import com.customer.application.port.inbound.AttachUserToCustomerUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.customer.internal.config.CustomerTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachUserToCustomerCommandHandler implements CommandHandler<AttachUserToCustomerCommand, CustomerResult> {

    private final AttachUserToCustomerUseCase attachUserToCustomerUseCase;

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
