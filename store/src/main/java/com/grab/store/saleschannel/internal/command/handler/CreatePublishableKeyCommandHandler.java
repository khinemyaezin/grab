package com.grab.store.saleschannel.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.saleschannel.internal.config.SalesChannelTransactional;
import com.saleschannel.application.model.write.CreatePublishableKeyCommand;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;
import com.saleschannel.application.port.inbound.CreatePublishableKeyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreatePublishableKeyCommandHandler
        implements CommandHandler<CreatePublishableKeyCommand, CreatePublishableKeyResult> {
    private final CreatePublishableKeyUseCase useCase;

    @Override
    @SalesChannelTransactional
    public CreatePublishableKeyResult handle(CreatePublishableKeyCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<CreatePublishableKeyCommand> getCommandType() {
        return CreatePublishableKeyCommand.class;
    }
}
