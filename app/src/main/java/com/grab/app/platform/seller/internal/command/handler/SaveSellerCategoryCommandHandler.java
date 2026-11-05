package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerCategoryCommands.SaveSellerCategoryCommand;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.SaveCategoryRecord;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SaveSellerCategoryCommandHandler
        implements CommandHandler<SaveSellerCategoryCommand, CategoryOutboundResult> {

    private final SellerCategoryOutboundPort outboundPort;

    public SaveSellerCategoryCommandHandler(SellerCategoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public CategoryOutboundResult handle(SaveSellerCategoryCommand command) {
        SaveCategoryRecord record = new SaveCategoryRecord(
                command.name(),
                command.parentId(),
                command.active(),
                command.listingAllowed(),
                command.c2cAllowed()
        );
        return outboundPort.saveCategory(record);
    }

    @Override
    public Class<SaveSellerCategoryCommand> getCommandType() {
        return SaveSellerCategoryCommand.class;
    }
}
