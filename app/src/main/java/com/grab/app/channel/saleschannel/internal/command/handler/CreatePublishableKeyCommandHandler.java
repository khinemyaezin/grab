package com.grab.app.channel.saleschannel.internal.command.handler;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.saleschannel.application.model.write.CreatePublishableKeyCommand;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;
import com.saleschannel.application.port.inbound.CreatePublishableKeyUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreatePublishableKeyCommandHandler
        implements CommandHandler<CreatePublishableKeyCommand, CreatePublishableKeyResult> {

    private final CreatePublishableKeyUseCase useCase;

    public CreatePublishableKeyCommandHandler(CreatePublishableKeyUseCase useCase) {
        this.useCase = useCase;
    }

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

