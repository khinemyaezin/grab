package com.grab.service.account.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.service.account.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.ExternalIdentityLinkResult;
import com.identity.application.model.write.LinkExternalIdentityCommand;
import com.identity.application.port.inbound.LinkExternalIdentityUseCase;
import org.springframework.stereotype.Component;

@Component
public class LinkExternalIdentityCommandHandler
        implements CommandHandler<LinkExternalIdentityCommand, ExternalIdentityLinkResult> {
    private final LinkExternalIdentityUseCase useCase;

    public LinkExternalIdentityCommandHandler(LinkExternalIdentityUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityTransactional
    public ExternalIdentityLinkResult handle(LinkExternalIdentityCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<LinkExternalIdentityCommand> getCommandType() {
        return LinkExternalIdentityCommand.class;
    }
}
