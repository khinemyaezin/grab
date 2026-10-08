package com.grab.store.saleschannel.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesChannelSecurityManifestStartupPublisher {
    private final CommandBus commandBus;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onStartup() {
        commandBus.dispatch(new PublishSalesChannelSecurityManifestCommand());
    }

    @Scheduled(initialDelayString = "${security.manifest.republish.initial-delay-ms:300000}",
            fixedDelayString = "${security.manifest.republish.fixed-delay-ms:300000}")
    public void onRepair() {
        commandBus.dispatch(new PublishSalesChannelSecurityManifestCommand());
    }
}
