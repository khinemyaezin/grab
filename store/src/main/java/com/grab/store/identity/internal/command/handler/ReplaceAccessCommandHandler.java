package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.ReplaceAccessUseCase;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.model.write.AccessAssignmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplaceAccessCommandHandler implements CommandHandler<ReplaceAccessCommand, AccessAssignmentResult> {

    private final ReplaceAccessUseCase replaceAccessUseCase;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(ReplaceAccessCommand command) {
        return replaceAccessUseCase.execute(command);
    }

    @Override
    public Class<ReplaceAccessCommand> getCommandType() {
        return ReplaceAccessCommand.class;
    }
}
