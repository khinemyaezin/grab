package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.RegisterScopeManifestCommand;
import com.identity.application.port.inbound.RegisterScopeManifestUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterScopeManifestCommandHandler
        implements CommandHandler<RegisterScopeManifestCommand, Void> {
    private final RegisterScopeManifestUseCase registerScopeManifestUseCase;

    @Override
    @IdentityTransactional
    public Void handle(RegisterScopeManifestCommand command) {
        registerScopeManifestUseCase.execute(command);
        return null;
    }

    @Override
    public Class<RegisterScopeManifestCommand> getCommandType() {
        return RegisterScopeManifestCommand.class;
    }
}
