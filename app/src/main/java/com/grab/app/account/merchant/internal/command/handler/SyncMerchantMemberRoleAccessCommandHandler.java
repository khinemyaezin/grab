package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.SyncMerchantMemberRoleAccessCommand;
import com.merchant.application.port.inbound.SyncMerchantMemberRoleAccessUseCase;
import org.springframework.stereotype.Component;

@Component
public class SyncMerchantMemberRoleAccessCommandHandler
        implements CommandHandler<SyncMerchantMemberRoleAccessCommand, Void> {

    private final SyncMerchantMemberRoleAccessUseCase syncMerchantMemberRoleAccessUseCase;

    public SyncMerchantMemberRoleAccessCommandHandler(
            SyncMerchantMemberRoleAccessUseCase syncMerchantMemberRoleAccessUseCase) {
        this.syncMerchantMemberRoleAccessUseCase = syncMerchantMemberRoleAccessUseCase;
    }

    @Override
    @MerchantTransactional
    public Void handle(SyncMerchantMemberRoleAccessCommand command) {
        syncMerchantMemberRoleAccessUseCase.execute(command);
        return null;
    }

    @Override
    public Class<SyncMerchantMemberRoleAccessCommand> getCommandType() {
        return SyncMerchantMemberRoleAccessCommand.class;
    }
}

