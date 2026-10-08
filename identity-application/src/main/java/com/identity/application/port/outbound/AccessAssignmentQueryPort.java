package com.identity.application.port.outbound;

import com.identity.application.model.read.AccessAssignmentView;

import java.time.Instant;
import java.util.List;

public interface AccessAssignmentQueryPort {
    List<AccessAssignmentView> findByUser(String userId);

    List<AccessAssignmentView> findEffectiveByUser(String userId, Instant now);
}
