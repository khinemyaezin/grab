package com.identity.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.application.model.write.RevalidateWaitingSecurityManifestsCommand;
import com.identity.application.port.inbound.RegisterSecurityManifestUseCase;
import com.identity.application.port.inbound.RevalidateWaitingSecurityManifestsUseCase;
import com.identity.domain.port.outbound.SecurityManifestRevisionRepository;
import com.identity.domain.security.SecurityManifestCandidate;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RevalidateWaitingSecurityManifestsService implements RevalidateWaitingSecurityManifestsUseCase {
    private static final Logger log = Loggers.getLogger(RevalidateWaitingSecurityManifestsService.class);

    private final SecurityManifestRevisionRepository revisions;
    private final RegisterSecurityManifestUseCase registration;

    @Override
    public void execute(RevalidateWaitingSecurityManifestsCommand command) {
        List<SecurityManifestCandidate> waitingCandidates = revisions.findWaiting();
        for (SecurityManifestCandidate candidate : waitingCandidates) {
            try {
                var registrationCommand = new RegisterSecurityManifestCommand(
                        candidate.manifest(), candidate.eventId());
                registration.execute(registrationCommand);
            } catch (Exception ex) {
                log.warn("Failed to revalidate waiting security manifest for event: {}", candidate.eventId(), ex);
            }
        }
    }
}
