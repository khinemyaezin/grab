package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.RegisterAuthorityManifestCommand;
import com.identity.application.port.inbound.RegisterAuthorityManifestUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterAuthorityManifestCommandHandler
        implements CommandHandler<RegisterAuthorityManifestCommand, Void> {

    private final RegisterAuthorityManifestUseCase registerAuthorityManifestUseCase;

    @Override
    @IdentityTransactional
    public Void handle(RegisterAuthorityManifestCommand command) {
        registerAuthorityManifestUseCase.execute(command);
        return null;
    }

    @Override
    public Class<RegisterAuthorityManifestCommand> getCommandType() {
        return RegisterAuthorityManifestCommand.class;
    }
}
