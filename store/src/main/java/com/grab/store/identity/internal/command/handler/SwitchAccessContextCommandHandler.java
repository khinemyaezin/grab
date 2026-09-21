package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.SwitchAccessContextUseCase;
import com.identity.application.model.write.SwitchAccessContextCommand;
import com.identity.application.model.write.AuthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SwitchAccessContextCommandHandler implements CommandHandler<SwitchAccessContextCommand, AuthResult> {

    private final SwitchAccessContextUseCase switchAccessContextUseCase;

    @Override
    @IdentityTransactional
    public AuthResult handle(SwitchAccessContextCommand command) {
        return switchAccessContextUseCase.execute(command);
    }

    @Override
    public Class<SwitchAccessContextCommand> getCommandType() {
        return SwitchAccessContextCommand.class;
    }
}
