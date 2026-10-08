package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventorySecurityManifestStartupPublisher {
    private final CommandBus commandBus;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onStartup() {
        commandBus.dispatch(new PublishInventorySecurityManifestCommand());
    }

    @Scheduled(initialDelayString = "${security.manifest.republish.initial-delay-ms:300000}",
            fixedDelayString = "${security.manifest.republish.fixed-delay-ms:300000}")
    public void onRepair() {
        commandBus.dispatch(new PublishInventorySecurityManifestCommand());
    }
}
