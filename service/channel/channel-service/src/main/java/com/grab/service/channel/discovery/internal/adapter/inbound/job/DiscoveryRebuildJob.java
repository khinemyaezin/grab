package com.grab.service.channel.discovery.internal.adapter.inbound.job;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * One-shot discovery projection rebuild, run as a Kubernetes Job (or manually) instead of on every pod start:
 * {@code JOBS_DISCOVERY_REBUILD_ENABLED=true java -jar channel-service.jar}. Each command processes one resumable batch; the job
 * loops until the run reports completion and then exits the JVM (0 on success, 1 on failure). It runs before
 * {@code ApplicationReadyEvent}, so bootstrap tasks do not start in the job pod.
 */
@Component
@ConditionalOnProperty(prefix = "jobs.discovery-rebuild", name = "enabled", havingValue = "true")
public class DiscoveryRebuildJob implements ApplicationRunner {

    private static final Logger log = Loggers.getLogger(DiscoveryRebuildJob.class);

    private final CommandBus commandBus;
    private final ConfigurableApplicationContext context;
    private final int batchSize;
    private final boolean exitOnCompletion;

    public DiscoveryRebuildJob(@Qualifier("channelCommandBus") CommandBus commandBus,
                               ConfigurableApplicationContext context,
                               @Value("${storefrontquery.rebuild.batch-size:100}") int batchSize,
                               @Value("${jobs.discovery-rebuild.exit-on-completion:true}") boolean exitOnCompletion) {
        this.commandBus = commandBus;
        this.context = context;
        this.batchSize = batchSize;
        this.exitOnCompletion = exitOnCompletion;
    }

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = rebuild();
        if (exitOnCompletion) {
            ExitCodeGenerator generator = () -> exitCode;
            int code = SpringApplication.exit(context, generator);
            System.exit(code);
        }
    }

    int rebuild() {
        try {
            RebuildProgressResult progress;
            do {
                var command = new RebuildDiscoveryProductsCommand(batchSize);
                progress = commandBus.dispatch(command);
                log.info("Discovery rebuild stage: {} processed: {} invalidated: {}",
                        progress.stage(), progress.processedCount(), progress.invalidatedCount());
            } while (!progress.complete());
            log.info("Discovery rebuild completed processed: {}", progress.processedCount());
            return 0;
        } catch (RuntimeException e) {
            log.error("Discovery rebuild failed", e);
            return 1;
        }
    }
}
