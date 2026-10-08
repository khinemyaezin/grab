package com.grab.store.identity.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.identity.application.model.write.RevalidateWaitingSecurityManifestsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentitySecurityManifestRevalidationScheduler {
    private final CommandBus commandBus;

    @Scheduled(fixedDelayString = "${security.manifest.revalidation.fixed-delay-ms:30000}")
    public void revalidate() {
        commandBus.dispatch(new RevalidateWaitingSecurityManifestsCommand());
    }
}
