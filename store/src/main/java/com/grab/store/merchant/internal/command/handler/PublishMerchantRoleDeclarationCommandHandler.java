package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.grab.store.shared.security.SecurityManifestPublicationRetryable;
import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;
import com.merchant.application.port.inbound.PublishMerchantRoleDeclarationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class PublishMerchantRoleDeclarationCommandHandler
        implements CommandHandler<PublishMerchantRoleDeclarationCommand, Void> {
    private final PublishMerchantRoleDeclarationUseCase useCase;

    @Override
    @SecurityManifestPublicationRetryable
    @MerchantTransactional(propagation = Propagation.REQUIRES_NEW)
    public Void handle(PublishMerchantRoleDeclarationCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<PublishMerchantRoleDeclarationCommand> getCommandType() {
        return PublishMerchantRoleDeclarationCommand.class;
    }
}
