package com.grab.app.channel.saleschannel.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.saleschannel.application.model.write.RevokePublishableKeyCommand;
import com.saleschannel.application.port.inbound.RevokePublishableKeyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevokePublishableKeyCommandHandler
        implements CommandHandler<RevokePublishableKeyCommand, Void> {
    private final RevokePublishableKeyUseCase useCase;

    @Override
    @SalesChannelTransactional
    public Void handle(RevokePublishableKeyCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<RevokePublishableKeyCommand> getCommandType() {
        return RevokePublishableKeyCommand.class;
    }
}
