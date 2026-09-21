package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.event.UserRegistrationIntegrationEventPublisher;
import com.identity.application.port.inbound.RegisterUseCase;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterCommandHandler implements CommandHandler<RegisterCommand, UserProfileResult> {

    private final RegisterUseCase registerUseCase;
    private final UserRegistrationIntegrationEventPublisher registrationEvents;

    @Override
    @IdentityTransactional
    public UserProfileResult handle(RegisterCommand command) {
        UserProfileResult result = registerUseCase.execute(command);
        return registrationEvents.afterRegistration(command, result);
    }

    @Override
    public Class<RegisterCommand> getCommandType() {
        return RegisterCommand.class;
    }
}
