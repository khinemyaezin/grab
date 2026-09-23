package com.identity.domain.policy;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;

import java.util.List;
import java.util.function.Supplier;

public interface RegistrationAccessPolicy {
    String platformCode();

    List<AccessAssignment> createAssignments(Id userId, Platform platform, Supplier<Id> idSupplier);

    default AccessAssignment createAssignment(Id assignmentId, Id userId, Platform platform) {
        List<AccessAssignment> assignments = createAssignments(userId, platform, () -> assignmentId);
        if (assignments.isEmpty()) {
            throw new IllegalStateException("Policy produced no assignments for platform: " + platformCode());
        }
        return assignments.get(0);
    }
}