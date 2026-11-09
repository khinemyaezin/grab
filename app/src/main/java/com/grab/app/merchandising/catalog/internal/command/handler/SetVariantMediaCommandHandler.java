package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.SetVariantMediaCommand;
import com.catalog.application.model.write.SetVariantMediaResult;
import com.catalog.application.port.inbound.SetVariantMediaUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SetVariantMediaCommandHandler implements CommandHandler<SetVariantMediaCommand, SetVariantMediaResult> {

    private final SetVariantMediaUseCase setVariantMediaUseCase;

    public SetVariantMediaCommandHandler(SetVariantMediaUseCase setVariantMediaUseCase) {
        this.setVariantMediaUseCase = setVariantMediaUseCase;
    }

    @Override
    @CatalogTransactional
    public SetVariantMediaResult handle(SetVariantMediaCommand command) {
        return setVariantMediaUseCase.execute(command);
    }

    @Override
    public Class<SetVariantMediaCommand> getCommandType() {
        return SetVariantMediaCommand.class;
    }
}
