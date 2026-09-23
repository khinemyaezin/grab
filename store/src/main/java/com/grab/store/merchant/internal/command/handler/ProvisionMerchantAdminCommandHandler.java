package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.ProvisionMerchantAdminCommand;
import com.merchant.application.port.inbound.ProvisionMerchantAdminUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProvisionMerchantAdminCommandHandler implements CommandHandler<ProvisionMerchantAdminCommand, MerchantMemberResult> {

    private final ProvisionMerchantAdminUseCase provisionMerchantAdminUseCase;

    @Override
    @MerchantTransactional
    public MerchantMemberResult handle(ProvisionMerchantAdminCommand command) {
        return provisionMerchantAdminUseCase.execute(command);
    }

    @Override
    public Class<ProvisionMerchantAdminCommand> getCommandType() {
        return ProvisionMerchantAdminCommand.class;
    }
}
