package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.LoginUseCase;
import com.identity.application.model.write.LoginCommand;
import com.identity.application.model.write.AuthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginCommandHandler implements CommandHandler<LoginCommand, AuthResult> {

    private final LoginUseCase loginUseCase;

    @Override
    @IdentityTransactional
    public AuthResult handle(LoginCommand command) {
        return loginUseCase.execute(command);
    }

    @Override
    public Class<LoginCommand> getCommandType() {
        return LoginCommand.class;
    }
}
