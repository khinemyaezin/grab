package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantSecurityManifestStartupPublisher {
    private final CommandBus commandBus;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onStartup() {
        commandBus.dispatch(new PublishMerchantSecurityManifestCommand());
    }

    @Scheduled(initialDelayString = "${security.manifest.republish.initial-delay-ms:300000}",
            fixedDelayString = "${security.manifest.republish.fixed-delay-ms:300000}")
    public void onRepair() {
        commandBus.dispatch(new PublishMerchantSecurityManifestCommand());
    }
}
