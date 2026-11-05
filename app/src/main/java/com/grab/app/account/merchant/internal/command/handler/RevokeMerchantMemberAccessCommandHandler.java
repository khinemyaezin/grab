package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.RevokeMerchantMemberAccessCommand;
import com.merchant.application.port.inbound.RevokeMerchantMemberAccessUseCase;
import org.springframework.stereotype.Component;

@Component
public class RevokeMerchantMemberAccessCommandHandler
        implements CommandHandler<RevokeMerchantMemberAccessCommand, Void> {

    private final RevokeMerchantMemberAccessUseCase revokeMerchantMemberAccessUseCase;

    public RevokeMerchantMemberAccessCommandHandler(
            RevokeMerchantMemberAccessUseCase revokeMerchantMemberAccessUseCase) {
        this.revokeMerchantMemberAccessUseCase = revokeMerchantMemberAccessUseCase;
    }

    @Override
    @MerchantTransactional
    public Void handle(RevokeMerchantMemberAccessCommand command) {
        revokeMerchantMemberAccessUseCase.execute(command);
        return null;
    }

    @Override
    public Class<RevokeMerchantMemberAccessCommand> getCommandType() {
        return RevokeMerchantMemberAccessCommand.class;
    }
}

