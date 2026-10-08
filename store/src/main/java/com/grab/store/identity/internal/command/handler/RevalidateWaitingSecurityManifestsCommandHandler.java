package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.RevalidateWaitingSecurityManifestsCommand;
import com.identity.application.port.inbound.RevalidateWaitingSecurityManifestsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevalidateWaitingSecurityManifestsCommandHandler
        implements CommandHandler<RevalidateWaitingSecurityManifestsCommand, Void> {
    private final RevalidateWaitingSecurityManifestsUseCase useCase;

    @Override
    @IdentityTransactional
    public Void handle(RevalidateWaitingSecurityManifestsCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<RevalidateWaitingSecurityManifestsCommand> getCommandType() {
        return RevalidateWaitingSecurityManifestsCommand.class;
    }
}
