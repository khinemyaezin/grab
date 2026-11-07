package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerCategoryCommands.DeleteSellerCategoryCommand;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteSellerCategoryCommandHandler
        implements CommandHandler<DeleteSellerCategoryCommand, Void> {

    private final SellerCategoryOutboundPort outboundPort;

    public DeleteSellerCategoryCommandHandler(SellerCategoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeleteSellerCategoryCommand command) {
        outboundPort.deleteCategory(command.categoryId());
        return null;
    }

    @Override
    public Class<DeleteSellerCategoryCommand> getCommandType() {
        return DeleteSellerCategoryCommand.class;
    }
}

