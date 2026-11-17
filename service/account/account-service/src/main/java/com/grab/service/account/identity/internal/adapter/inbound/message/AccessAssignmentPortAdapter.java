package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.service.contract.message.account.identity.AccessAssignmentPort;
import com.grab.framework.id.Id;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.List;

@Component
@RmiExport
public class AccessAssignmentPortAdapter implements AccessAssignmentPort {

    private final AccessAssignmentReadAdapter reads;
    private final AccessAssignmentWriteAdapter writes;

    public AccessAssignmentPortAdapter(
            AccessAssignmentReadAdapter reads,
            AccessAssignmentWriteAdapter writes
    ) {
        this.reads = reads;
        this.writes = writes;
    }

    @Override
    public AssignmentMessageResult assignAccess(AssignAccessMessage message) {
        return writes.assignAccess(message);
    }

    @Override
    public AssignmentMessageResult changeAssignmentStatus(ChangeAssignmentStatusMessage message) {
        return writes.changeAssignmentStatus(message);
    }

    @Override
    public AuthTokenResult switchAccessContext(SwitchContextMessage message) {
        return writes.switchAccessContext(message);
    }

    @Override
    public List<AssignmentMessageResult> listAssignmentsForUser(Id userId, String actorScopeKey, String actorScopeId) {
        return reads.listAssignmentsForUser(userId, actorScopeKey, actorScopeId);
    }

    @Override
    public List<ContextMessageResult> listAvailableContexts(Id userId) {
        return reads.listAvailableContexts(userId);
    }

    @Override
    public AccessWriteAcknowledgement replaceAccess(ReplaceAccessMessage message) {
        return writes.replaceAccess(message);
    }

    @Override
    public AccessWriteAcknowledgement revokeAccess(RevokeAccessMessage message) {
        return writes.revokeAccess(message);
    }

    @Override
    public AccessWriteAcknowledgement revokeSessionsByScope(RevokeSessionsByScopeMessage message) {
        return writes.revokeSessionsByScope(message);
    }
}
