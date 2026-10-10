package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.shared.security.SecurityManifestPublicationRetryable;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class PublishMerchantSecurityManifestCommandHandler
        implements CommandHandler<PublishMerchantSecurityManifestCommand, Void> {
    private final PublishMerchantSecurityManifestUseCase useCase;

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
