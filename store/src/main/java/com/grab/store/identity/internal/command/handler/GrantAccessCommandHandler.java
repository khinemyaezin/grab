package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.GrantAccessUseCase;
import com.identity.application.model.write.GrantAccessCommand;
import com.identity.application.model.write.AccessAssignmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantAccessCommandHandler implements CommandHandler<GrantAccessCommand, AccessAssignmentResult> {

    private final GrantAccessUseCase grantAccessUseCase;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(GrantAccessCommand command) {
        return grantAccessUseCase.execute(command);
    }

    @Override
    public Class<GrantAccessCommand> getCommandType() {
        return GrantAccessCommand.class;
    }
}
