package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.ChangeAccessStatusUseCase;
import com.identity.application.model.write.ChangeAccessStatusCommand;
import com.identity.application.model.write.AccessAssignmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeAccessStatusCommandHandler implements CommandHandler<ChangeAccessStatusCommand, AccessAssignmentResult> {

    private final ChangeAccessStatusUseCase changeAccessStatusUseCase;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(ChangeAccessStatusCommand command) {
        return changeAccessStatusUseCase.execute(command);
    }

    @Override
    public Class<ChangeAccessStatusCommand> getCommandType() {
        return ChangeAccessStatusCommand.class;
    }
}
