package com.identity.adapter.persistence.adapter;

import com.identity.adapter.persistence.entity.SecurityCatalogStateEntity;
import com.identity.adapter.persistence.entity.SecurityManifestModuleEntity;
import com.identity.adapter.persistence.entity.SecurityManifestRevisionEntity;
import com.identity.adapter.persistence.repository.jpa.*;
import com.identity.application.model.read.SecurityCatalogStatusView;
import com.identity.application.model.read.WaitingSecurityManifestView;
import com.identity.application.port.outbound.SecurityManifestQueryPort;
import com.identity.adapter.persistence.specification.jpa.SecurityManifestWaitingSpecification;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class SecurityManifestQueryAdapter implements SecurityManifestQueryPort {
    private final SecurityManifestWaitingSpecification waiting;
    private final SecurityManifestRevisionJpaRepository revisions;
    private final SecurityManifestModuleJpaRepository modules;
    private final SecurityCatalogStateJpaRepository states;
    private final SecurityManifestConflictJpaRepository conflicts;

    @Override
    public List<WaitingSecurityManifestView> findWaiting(int limit) {
        return waiting.findWaiting(limit);
    }
    @Override
    public SecurityCatalogStatusView status(String moduleKey) {
        var module = modules.findById(moduleKey);
        int appliedRevision = module.map(SecurityManifestModuleEntity::getAppliedRevision).orElse(0);
        String appliedDigest = module.map(SecurityManifestModuleEntity::getAppliedDigest).orElse(null);
        var catalogState = states.findById(1L);
        long catalogRevision = catalogState.map(SecurityCatalogStateEntity::getCatalogRevision).orElse(0L);
        List<String> accepted = List.of("APPLIED", "WAITING_DEPENDENCY");
        int highest = revisions.findTopByModuleKeyAndStatusInOrderByRevisionDesc(moduleKey, accepted)
                .map(SecurityManifestRevisionEntity::getRevision).orElse(0);
        String waitingSince = revisions.findTopByModuleKeyAndStatusOrderByReceivedAtAsc(moduleKey, "WAITING_DEPENDENCY")
                .map(entity -> entity.getReceivedAt().toString()).orElse(null);
        long conflictCount = conflicts.countByModuleKey(moduleKey);
        return new SecurityCatalogStatusView(moduleKey, appliedRevision, appliedDigest, catalogRevision,
                highest, waitingSince, conflictCount);
    }
}
