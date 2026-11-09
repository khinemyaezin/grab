package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.ReplaceProductDescriptionsCommand;
import com.catalog.application.model.write.ProductDescriptionsResult;
import com.catalog.application.port.inbound.ReplaceProductDescriptionsUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ReplaceProductDescriptionsCommandHandler implements CommandHandler<ReplaceProductDescriptionsCommand, ProductDescriptionsResult> {

    private final ReplaceProductDescriptionsUseCase replaceProductDescriptionsUseCase;

    public ReplaceProductDescriptionsCommandHandler(ReplaceProductDescriptionsUseCase replaceProductDescriptionsUseCase) {
        this.replaceProductDescriptionsUseCase = replaceProductDescriptionsUseCase;
    }

    @Override
    @CatalogTransactional
    public ProductDescriptionsResult handle(ReplaceProductDescriptionsCommand command) {
        return replaceProductDescriptionsUseCase.execute(command);
    }

    @Override
    public Class<ReplaceProductDescriptionsCommand> getCommandType() {
        return ReplaceProductDescriptionsCommand.class;
    }
}
