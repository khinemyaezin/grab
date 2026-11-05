package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import com.identity.application.port.inbound.RegisterUseCase;
import org.springframework.stereotype.Component;

@Component
public class RegisterCommandHandler implements CommandHandler<RegisterCommand, UserProfileResult> {

    private final RegisterUseCase registerUseCase;

    public RegisterCommandHandler(RegisterUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    @Override
    @IdentityTransactional
    public UserProfileResult handle(RegisterCommand command) {
        return registerUseCase.execute(command);
    }

    @Override
    public Class<RegisterCommand> getCommandType() {
        return RegisterCommand.class;
    }
}
