package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RecordMemberProvisioningResultCommand;
import com.merchant.application.port.inbound.RecordMemberProvisioningResultUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecordMemberProvisioningResultCommandHandler implements CommandHandler<RecordMemberProvisioningResultCommand, MerchantMemberResult> {

    private final RecordMemberProvisioningResultUseCase recordMemberProvisioningResultUseCase;

    @Override
    @MerchantTransactional
    public MerchantMemberResult handle(RecordMemberProvisioningResultCommand command) {
        return recordMemberProvisioningResultUseCase.execute(command);
    }

    @Override
    public Class<RecordMemberProvisioningResultCommand> getCommandType() {
        return RecordMemberProvisioningResultCommand.class;
    }
}
