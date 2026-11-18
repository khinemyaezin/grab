package com.grab.service.account.identity.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.scheduling.infrastructure.ClusterSingletonJob;
import com.identity.application.model.read.ListWaitingSecurityManifestCandidatesQuery;
import com.identity.application.model.write.RevalidateSecurityManifestCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class IdentitySecurityManifestRevalidationScheduler {

    private static final String REVALIDATION_JOB_NAME = "identity.security-manifest.revalidation";
    private static final SingletonJob REVALIDATION_JOB = new SingletonJob(
            REVALIDATION_JOB_NAME, Duration.ofMinutes(5), Duration.ofSeconds(20));

    private static final Logger log = Loggers.getLogger(IdentitySecurityManifestRevalidationScheduler.class);
    private final QueryBus queries;
    private final CommandBus commands;
    private final int batchSize;
    private final SingletonJobExecutor singletonJobExecutor;

    public IdentitySecurityManifestRevalidationScheduler(
            @Qualifier("accountQueryBus") QueryBus queries,
            @Qualifier("accountCommandBus") CommandBus commands,
            @Value("${security.manifest.revalidation.batch-size:100}") int batchSize,
            @Qualifier("identitySingletonJobExecutor") SingletonJobExecutor singletonJobExecutor) {
        this.queries = queries;
        this.commands = commands;
        this.batchSize = batchSize;
        this.singletonJobExecutor = singletonJobExecutor;
    }

    @ClusterSingletonJob(REVALIDATION_JOB_NAME)
    @Scheduled(fixedDelayString = "${security.manifest.revalidation.fixed-delay-ms:30000}")
    public void revalidate() {
        singletonJobExecutor.execute(REVALIDATION_JOB, this::revalidateBatch);
    }

    private void revalidateBatch() {
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

