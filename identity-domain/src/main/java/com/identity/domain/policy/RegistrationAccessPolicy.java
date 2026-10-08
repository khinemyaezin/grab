package com.identity.domain.policy;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessAssignment;

import java.util.List;
import java.util.function.Supplier;

public interface RegistrationAccessPolicy {
    String policyCode();

    List<AccessAssignment> createAssignments(Id userId, Supplier<Id> idSupplier);

    default AccessAssignment createAssignment(Id assignmentId, Id userId) {
        List<AccessAssignment> assignments = createAssignments(userId, () -> assignmentId);
        if (assignments.isEmpty()) {
            throw new IllegalStateException("Policy produced no assignments for: " + policyCode());
        }
        return assignments.get(0);
    }
}