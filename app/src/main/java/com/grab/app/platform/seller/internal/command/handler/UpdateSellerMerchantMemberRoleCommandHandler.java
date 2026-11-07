package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.UpdateSellerMerchantMemberRoleCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.UpdateMemberRoleRecord;
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

