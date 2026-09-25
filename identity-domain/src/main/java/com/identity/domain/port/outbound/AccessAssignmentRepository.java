package com.identity.domain.port.outbound;

import com.grab.framework.id.Id;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.valueobject.AccessScope;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccessAssignmentRepository {
    Optional<AccessAssignment> findById(Id id);

    Optional<AccessAssignment> findCurrent(
            Id userId,
            String roleCode,
            AccessScope scope
    );

    List<AccessAssignment> findEffectiveByUser(Id userId, Instant now);

    List<AccessAssignment> findCurrentByUserAndScope(
            Id userId,
            AccessScope scope
    );

    List<AccessAssignment> findByUser(Id userId);

    boolean existsEffective(Id userId, String roleCode, AccessScope scope, Instant now);

    boolean existsCurrent(Id userId, String roleCode, AccessScope scope);

    AccessAssignment save(AccessAssignment assignment);
}
