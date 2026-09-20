package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.ProductMediaResult;
import com.catalog.application.model.write.ReplaceProductMediaCommand;
import com.catalog.application.port.inbound.ReplaceProductMediaUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplaceProductMediaCommandHandler implements CommandHandler<ReplaceProductMediaCommand, ProductMediaResult> {

    private final ReplaceProductMediaUseCase replaceProductMediaUseCase;

    @Override
    @CatalogTransactional
    public ProductMediaResult handle(ReplaceProductMediaCommand command) {
        return replaceProductMediaUseCase.execute(command);
    }

    @Override
    public Class<ReplaceProductMediaCommand> getCommandType() {
        return ReplaceProductMediaCommand.class;
    }
}
