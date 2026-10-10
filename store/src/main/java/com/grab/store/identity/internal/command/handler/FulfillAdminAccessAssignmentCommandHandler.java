package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.FulfillAdminAccessAssignmentCommand;
import com.identity.application.port.inbound.FulfillAdminAccessAssignmentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FulfillAdminAccessAssignmentCommandHandler
        implements CommandHandler<FulfillAdminAccessAssignmentCommand, AccessAssignmentResult> {

    private final FulfillAdminAccessAssignmentUseCase fulfillAdminAccessAssignmentUseCase;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(FulfillAdminAccessAssignmentCommand command) {
        return fulfillAdminAccessAssignmentUseCase.execute(command);
    }

    @Override
    public Class<FulfillAdminAccessAssignmentCommand> getCommandType() {
        return FulfillAdminAccessAssignmentCommand.class;
    }
}
