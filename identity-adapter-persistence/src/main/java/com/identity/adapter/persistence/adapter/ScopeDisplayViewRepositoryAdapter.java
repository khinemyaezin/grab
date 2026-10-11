package com.identity.adapter.persistence.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.adapter.persistence.entity.ScopeDisplayEntity;
import com.identity.adapter.persistence.repository.jpa.ScopeDisplayJpaRepository;
import com.identity.application.port.outbound.ScopeDisplayViewRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScopeDisplayViewRepositoryAdapter implements ScopeDisplayViewRepository {
    private final ScopeDisplayJpaRepository displays;
    private final PersistenceExecutor executor;

    @Override
    public void update(String scopeKey, Id scopeId, String title, String subtitle, long sourceRevision) {
        String scopeIdValue = scopeId.getValue();
        executor.command("ScopeDisplay", () -> {
            ScopeDisplayEntity entity = displays.findByScopeKeyAndScopeId(scopeKey, scopeIdValue).orElse(null);
            if (entity == null) {
                ScopeDisplayEntity newEntity = new ScopeDisplayEntity();
                newEntity.setScopeKey(scopeKey);
                newEntity.setScopeId(scopeIdValue);
                newEntity.setTitle(title);
                newEntity.setSubtitle(subtitle);
                newEntity.setSourceRevision(sourceRevision);
                displays.saveAndFlush(newEntity);
                return;
            }
            if (sourceRevision > entity.getSourceRevision()) {
                entity.setTitle(title);
                entity.setSubtitle(subtitle);
                entity.setSourceRevision(sourceRevision);
                displays.saveAndFlush(entity);
            }
        });
    }
}
