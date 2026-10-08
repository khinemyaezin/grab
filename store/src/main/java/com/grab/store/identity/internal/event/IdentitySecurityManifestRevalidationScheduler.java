package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.identity.application.model.read.ListWaitingSecurityManifestCandidatesQuery;
import com.identity.application.model.write.RevalidateSecurityManifestCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IdentitySecurityManifestRevalidationScheduler {
    private static final Logger log = Loggers.getLogger(IdentitySecurityManifestRevalidationScheduler.class);
    private final QueryBus queries;
    private final CommandBus commands;
    private final int batchSize;

    public IdentitySecurityManifestRevalidationScheduler(QueryBus queries, CommandBus commands,
            @Value("${security.manifest.revalidation.batch-size:100}") int batchSize) {
        this.queries = queries;
        this.commands = commands;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${security.manifest.revalidation.fixed-delay-ms:30000}")
    public void revalidate() {
        var query = new ListWaitingSecurityManifestCandidatesQuery(batchSize);
        var candidates = queries.dispatch(query);
        for (var candidate : candidates) {
            var command = new RevalidateSecurityManifestCommand(candidate.moduleKey(), candidate.securityRevision());
            try {
                commands.dispatch(command);
            } catch (RuntimeException exception) {
                log.warn("Security manifest revalidation failed module: {}, revision: {}",
                        candidate.moduleKey(), candidate.securityRevision(), exception);
            }
        }
    }
}
