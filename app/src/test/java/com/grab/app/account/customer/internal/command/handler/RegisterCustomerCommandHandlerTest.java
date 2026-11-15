package com.grab.app.account.customer.internal.command.handler;

import com.customer.application.model.write.RegisterCustomerCommand;
import com.customer.application.port.inbound.RegisterCustomerUseCase;
import com.grab.framework.id.Id;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RegisterCustomerCommandHandlerTest {

    @Test
    void commandOutsideOutboxDeliveryExecutesUseCase() {
        RegisterCustomerUseCase useCase = mock(RegisterCustomerUseCase.class);
        RegisterCustomerCommandHandler handler = new RegisterCustomerCommandHandler(useCase);
        Id userId = mock(Id.class);
        RegisterCustomerCommand command = new RegisterCustomerCommand(userId, "a@b.c", "a@b.c");

        handler.handle(command);

        verify(useCase).execute(command);
    }
}
