package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.SyncMerchantMemberRoleAccessCommand;
import com.merchant.application.port.inbound.SyncMerchantMemberRoleAccessUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SyncMerchantMemberRoleAccessCommandHandler
        implements CommandHandler<SyncMerchantMemberRoleAccessCommand, Void> {

    private final SyncMerchantMemberRoleAccessUseCase syncMerchantMemberRoleAccessUseCase;

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
