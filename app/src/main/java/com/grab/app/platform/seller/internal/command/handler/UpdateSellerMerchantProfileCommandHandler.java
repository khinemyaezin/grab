package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.UpdateSellerMerchantProfileCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.UpdateProfileRecord;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateSellerMerchantProfileCommandHandler
        implements CommandHandler<UpdateSellerMerchantProfileCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public UpdateSellerMerchantProfileCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(UpdateSellerMerchantProfileCommand command) {
        UpdateProfileRecord record = new UpdateProfileRecord(
                command.merchantId(),
                command.actorId(),
                command.legalName(),
                command.displayName(),
                command.registrationCountryCode(),
                command.registrationNumber(),
                command.contactEmail(),
                command.contactPhone(),
                command.addressLine1(),
                command.addressLine2(),
                command.addressCity(),
                command.addressRegion(),
                command.addressPostalCode(),
                command.addressCountryCode()
        );
        return outboundPort.updateProfile(record);
    }

    @Override
    public Class<UpdateSellerMerchantProfileCommand> getCommandType() {
        return UpdateSellerMerchantProfileCommand.class;
    }
}

