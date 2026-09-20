package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.ProductDescriptionsResult;
import com.catalog.application.command.ReplaceProductDescriptionsCommand;
import com.catalog.application.port.inbound.ReplaceProductDescriptionsUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplaceProductDescriptionsCommandHandler implements CommandHandler<ReplaceProductDescriptionsCommand, ProductDescriptionsResult> {

    private final ReplaceProductDescriptionsUseCase replaceProductDescriptionsUseCase;

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
