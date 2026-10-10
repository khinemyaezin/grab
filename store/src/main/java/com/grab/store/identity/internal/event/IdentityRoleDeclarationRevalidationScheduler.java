package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.identity.application.model.read.ListWaitingRoleDeclarationsQuery;
import com.identity.application.model.read.WaitingRoleDeclarationView;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IdentityRoleDeclarationRevalidationScheduler {
    private static final Logger log = Loggers.getLogger(IdentityRoleDeclarationRevalidationScheduler.class);
    private final QueryBus queries;
    private final CommandBus commands;
    private final int batchSize;

    public IdentityRoleDeclarationRevalidationScheduler(
            QueryBus queries,
            CommandBus commands,
            @Value("${security.role-declaration.revalidation.batch-size:100}") int batchSize
    ) {
        this.queries = queries;
        this.commands = commands;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${security.role-declaration.revalidation.fixed-delay-ms:30000}")
    public void revalidate() {
        ListWaitingRoleDeclarationsQuery query = new ListWaitingRoleDeclarationsQuery(batchSize);
        List<WaitingRoleDeclarationView> candidates = queries.dispatch(query);
        for (WaitingRoleDeclarationView candidate : candidates) {
            RegisterRoleDeclarationCommand command = new RegisterRoleDeclarationCommand(
                    candidate.declaration(), candidate.eventId(), candidate.contentDigest(), candidate.receivedAt(), true);
            try {
                commands.dispatch(command);
            } catch (RuntimeException exception) {
                log.warn("Role declaration revalidation failed owner={} roleCode={}",
                        candidate.declaration().owner(), candidate.declaration().roleCode(), exception);
            }
        }
    }
}
