package com.grab.service.account.merchant.internal.command.handler;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.grab.service.account.merchant.internal.config.MerchantTransactional;
import com.grab.service.account.shared.security.SecurityManifestPublicationRetryable;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class PublishMerchantSecurityManifestCommandHandler
        implements CommandHandler<PublishMerchantSecurityManifestCommand, PublicationResult> {

    private final PublishMerchantSecurityManifestUseCase useCase;

    public PublishMerchantSecurityManifestCommandHandler(PublishMerchantSecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SecurityManifestPublicationRetryable
    @MerchantTransactional(propagation = Propagation.REQUIRES_NEW)
    public PublicationResult handle(PublishMerchantSecurityManifestCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<PublishMerchantSecurityManifestCommand> getCommandType() {
        return PublishMerchantSecurityManifestCommand.class;
    }
}

