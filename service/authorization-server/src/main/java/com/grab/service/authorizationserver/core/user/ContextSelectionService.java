package com.grab.service.authorizationserver.core.user;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.service.authorizationserver.port.outbound.ScimClientPort;
import com.grab.service.contract.message.account.scim.ScimAccessContext;
import com.grab.service.contract.message.account.scim.ScimAccessContextExtension;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ContextSelectionService {
    private static final Logger LOG = Loggers.getLogger(ContextSelectionService.class);

    private final IamUserRepository users;
    private final IamAccessContextRepository contextRepo;
    private final ScimClientPort scimClient;

    public ContextSelectionService(IamUserRepository users,
                                   IamAccessContextRepository contextRepo,
                                   ScimClientPort scimClient) {
        this.users = users;
        this.contextRepo = contextRepo;
        this.scimClient = scimClient;
    }

    public List<IamAccessContext> getOrSyncContexts(String iamUserId) {
        IamUser user = users.findById(iamUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + iamUserId));

        String lookupId = user.platformUserId() != null && !user.platformUserId().isBlank()
                ? user.platformUserId()
                : user.id();

        try {
            ScimAccessContextExtension extension = scimClient.getUserAccessContexts(lookupId);
            if (extension != null && extension.contexts() != null) {
                List<IamAccessContext> synced = new ArrayList<>();
                for (ScimAccessContext scimCtx : extension.contexts()) {
                    synced.add(new IamAccessContext(
                            UUID.randomUUID().toString(),
                            iamUserId,
                            scimCtx.assignmentId(),
                            scimCtx.scopeKey(),
                            scimCtx.scopeId(),
                            scimCtx.role(),
                            scimCtx.displayName(),
                            scimCtx.authorities(),
                            Instant.now()
                    ));
                }
                contextRepo.replaceUserContexts(iamUserId, synced);
                return synced;
            }
        } catch (Exception e) {
            LOG.warn("Failed to sync access contexts from account-service over SCIM: {}", e.getMessage());
        }

        return contextRepo.findByUserId(iamUserId);
    }

    public SelectedContext resolveSelection(String iamUserId, String assignmentId) {
        IamUser user = users.findById(iamUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + iamUserId));

        List<IamAccessContext> available = getOrSyncContexts(iamUserId);
        IamAccessContext matched = available.stream()
                .filter(ctx -> ctx.assignmentId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid assignment ID: " + assignmentId));

        String platformUserId = user.platformUserId() != null ? user.platformUserId() : user.id();
        return new SelectedContext(
                matched.assignmentId(),
                platformUserId,
                matched.scopeKey(),
                matched.scopeId(),
                matched.role(),
                matched.displayName(),
                matched.authorities(),
                false
        );
    }
}
