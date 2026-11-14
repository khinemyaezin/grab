package com.grab.app.account.customer.internal.command.handler;

import com.customer.application.model.write.RegisterCustomerCommand;
import com.customer.application.port.inbound.RegisterCustomerUseCase;
import com.grab.framework.id.Id;
import com.grab.framework.inbox.ProcessedEventRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterCustomerCommandHandlerTest {

    private final RegisterCustomerUseCase useCase = mock(RegisterCustomerUseCase.class);
    private final ProcessedEventRepository inbox = mock(ProcessedEventRepository.class);
    private final RegisterCustomerCommandHandler handler = new RegisterCustomerCommandHandler(useCase, inbox);
    private final Id userId = mock(Id.class);

    @Test
    void duplicateDelivery_skipsUseCase() {
        UUID eventId = UUID.randomUUID();
        RegisterCustomerCommand command = new RegisterCustomerCommand(userId, "a@b.c", "a@b.c", eventId);
        when(inbox.markProcessed(RegisterCustomerCommandHandler.CONSUMER, eventId)).thenReturn(false);

        assertNull(handler.handle(command));
        verify(useCase, never()).execute(any());
    }

    @Test
    void firstDelivery_marksThenExecutes() {
        UUID eventId = UUID.randomUUID();
        RegisterCustomerCommand command = new RegisterCustomerCommand(userId, "a@b.c", "a@b.c", eventId);
        when(inbox.markProcessed(RegisterCustomerCommandHandler.CONSUMER, eventId)).thenReturn(true);

        handler.handle(command);

        verify(inbox).markProcessed(RegisterCustomerCommandHandler.CONSUMER, eventId);
        verify(useCase).execute(command);
    }

    @Test
    void commandOutsideOutboxDelivery_bypassesInbox() {
        RegisterCustomerCommand command = new RegisterCustomerCommand(userId, "a@b.c", "a@b.c");

        handler.handle(command);

        verify(inbox, never()).markProcessed(anyString(), any());
        verify(useCase).execute(command);
    }
}
