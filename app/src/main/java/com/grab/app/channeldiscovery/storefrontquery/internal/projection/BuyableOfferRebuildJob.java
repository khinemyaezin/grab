package com.grab.app.channeldiscovery.storefrontquery.internal.projection;

import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "storefrontquery.rebuild", name = "on-startup", havingValue = "true")
public class BuyableOfferRebuildJob {
    private final CommandBus commandBus;
    private final TaskExecutor executor;
    private final int batchSize;

    public BuyableOfferRebuildJob(
            CommandBus commandBus,
            @Qualifier("storefrontRebuildExecutor") TaskExecutor executor,
            @Value("${storefrontquery.rebuild.batch-size:100}") int batchSize
    ) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("Rebuild batch size must be positive.");
        }
        this.commandBus = commandBus;
        this.executor = executor;
        this.batchSize = batchSize;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildOnStartup() {
        executor.execute(this::rebuildUntilComplete);
    }

    private void rebuildUntilComplete() {
        boolean complete = false;
        while (!complete) {
            RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(batchSize);
            RebuildProgressResult progress = commandBus.dispatch(command);
            complete = progress.complete();
        }
    }
}
