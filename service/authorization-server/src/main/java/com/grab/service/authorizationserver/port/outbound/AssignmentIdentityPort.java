package com.grab.service.authorizationserver.port.outbound;

import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.Subject;

import java.util.Set;

public interface AssignmentIdentityPort {
    State resolve(String userId, String assignmentId, boolean selectSingleContext);

    record State(Subject subject, String email, String assignmentId, String scopeKey, String scopeId,
                 Set<String> roles, Set<String> authorities, long merchantRevision, long storefrontRevision) {}
}
