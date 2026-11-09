package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.app.shared.security.SecurityManifestPublicationRetryable;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class PublishMerchantSecurityManifestCommandHandler
        implements CommandHandler<PublishMerchantSecurityManifestCommand, Void> {

    private final PublishMerchantSecurityManifestUseCase useCase;

    public PublishMerchantSecurityManifestCommandHandler(PublishMerchantSecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SecurityManifestPublicationRetryable
    @MerchantTransactional(propagation = Propagation.REQUIRES_NEW)
    public Void handle(PublishMerchantSecurityManifestCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<PublishMerchantSecurityManifestCommand> getCommandType() {
        return PublishMerchantSecurityManifestCommand.class;
    }
}

