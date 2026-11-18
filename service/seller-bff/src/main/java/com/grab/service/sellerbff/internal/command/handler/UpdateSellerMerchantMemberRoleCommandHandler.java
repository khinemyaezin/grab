package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.UpdateSellerMerchantMemberRoleCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.UpdateMemberRoleRecord;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateSellerMerchantMemberRoleCommandHandler
        implements CommandHandler<UpdateSellerMerchantMemberRoleCommand, MerchantMemberOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public UpdateSellerMerchantMemberRoleCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantMemberOutboundResult handle(UpdateSellerMerchantMemberRoleCommand command) {
        UpdateMemberRoleRecord record = new UpdateMemberRoleRecord(
                command.merchantId(),
                command.memberId(),
                command.actorId(),
                command.newRole()
        );
        return outboundPort.updateMemberRole(record);
    }

    @Override
    public Class<UpdateSellerMerchantMemberRoleCommand> getCommandType() {
        return UpdateSellerMerchantMemberRoleCommand.class;
    }
}

