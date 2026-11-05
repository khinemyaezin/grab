package com.grab.app.account.merchant.internal.command.handler;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.ProvisionMerchantAdminCommand;
import com.merchant.application.port.inbound.ProvisionMerchantAdminUseCase;
import org.springframework.stereotype.Component;

@Component
public class ProvisionMerchantAdminCommandHandler
        implements CommandHandler<ProvisionMerchantAdminCommand, MerchantMemberResult> {

    private final ProvisionMerchantAdminUseCase provisionMerchantAdminUseCase;

    public ProvisionMerchantAdminCommandHandler(ProvisionMerchantAdminUseCase provisionMerchantAdminUseCase) {
        this.provisionMerchantAdminUseCase = provisionMerchantAdminUseCase;
    }

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
