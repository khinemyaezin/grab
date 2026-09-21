package com.grab.store.customer.internal.command.handler;

import com.customer.application.model.write.CustomerResult;
import com.customer.application.model.write.SuspendCustomerCommand;
import com.customer.application.port.inbound.SuspendCustomerUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.customer.internal.config.CustomerTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuspendCustomerCommandHandler implements CommandHandler<SuspendCustomerCommand, CustomerResult> {

    private final SuspendCustomerUseCase suspendCustomerUseCase;

    @Override
    @CustomerTransactional
    public CustomerResult handle(SuspendCustomerCommand command) {
        return suspendCustomerUseCase.execute(command);
    }

    @Override
    public Class<SuspendCustomerCommand> getCommandType() {
        return SuspendCustomerCommand.class;
    }
}
