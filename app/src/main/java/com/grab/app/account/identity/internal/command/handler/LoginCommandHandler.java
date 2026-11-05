package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.LoginCommand;
import com.identity.application.port.inbound.LoginUseCase;
import org.springframework.stereotype.Component;

@Component
public class LoginCommandHandler implements CommandHandler<LoginCommand, AuthResult> {

    private final LoginUseCase loginUseCase;

    public LoginCommandHandler(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

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

