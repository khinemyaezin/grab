package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.SetVariantMediaCommand;
import com.catalog.application.command.SetVariantMediaResult;
import com.catalog.application.port.inbound.SetVariantMediaUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetVariantMediaCommandHandler implements CommandHandler<SetVariantMediaCommand, SetVariantMediaResult> {

    private final SetVariantMediaUseCase setVariantMediaUseCase;

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
