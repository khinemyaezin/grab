package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.ReplaceProductMediaCommand;
import com.catalog.application.model.write.ProductMediaResult;
import com.catalog.application.port.inbound.ReplaceProductMediaUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ReplaceProductMediaCommandHandler implements CommandHandler<ReplaceProductMediaCommand, ProductMediaResult> {

    private final ReplaceProductMediaUseCase replaceProductMediaUseCase;

    public ReplaceProductMediaCommandHandler(ReplaceProductMediaUseCase replaceProductMediaUseCase) {
        this.replaceProductMediaUseCase = replaceProductMediaUseCase;
    }

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
