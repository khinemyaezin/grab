package com.grab.app.account.merchant.internal.command.handler;

import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;
import com.merchant.application.port.inbound.PublishMerchantRoleDeclarationUseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PublishMerchantRoleDeclarationCommandHandlerTest {

    @Test
    void handleDelegatesToUseCase() {
        PublishMerchantRoleDeclarationUseCase useCase = mock(PublishMerchantRoleDeclarationUseCase.class);
        PublishMerchantRoleDeclarationCommandHandler handler = new PublishMerchantRoleDeclarationCommandHandler(useCase);
        PublishMerchantRoleDeclarationCommand command = new PublishMerchantRoleDeclarationCommand();

        Void result = handler.handle(command);

        assertNull(result);
        verify(useCase).execute(command);
    }

    @Test
    void getCommandTypeReturnsExpectedType() {
        PublishMerchantRoleDeclarationUseCase useCase = mock(PublishMerchantRoleDeclarationUseCase.class);
        PublishMerchantRoleDeclarationCommandHandler handler = new PublishMerchantRoleDeclarationCommandHandler(useCase);

        assertEquals(PublishMerchantRoleDeclarationCommand.class, handler.getCommandType());
    }
}

