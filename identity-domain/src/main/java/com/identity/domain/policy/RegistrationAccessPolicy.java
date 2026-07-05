package com.identity.domain.policy;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;

public interface RegistrationAccessPolicy {
    String platformCode();

    AccessAssignment createAssignment(Id assignmentId, Id userId, Platform platform);
}