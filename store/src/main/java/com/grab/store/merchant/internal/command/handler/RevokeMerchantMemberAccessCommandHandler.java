package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.RevokeMerchantMemberAccessCommand;
import com.merchant.application.port.inbound.RevokeMerchantMemberAccessUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevokeMerchantMemberAccessCommandHandler
        implements CommandHandler<RevokeMerchantMemberAccessCommand, Void> {

    private final RevokeMerchantMemberAccessUseCase revokeMerchantMemberAccessUseCase;

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
