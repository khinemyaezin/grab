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
    public void update(Id scopeId, String name) {
        String scopeIdValue = scopeId.getValue();
        executor.command("ScopeDisplay", () -> {
            ScopeDisplayEntity entity = displays.findByScopeId(scopeIdValue).orElse(null);
            if (entity == null) {
                ScopeDisplayEntity newEntity = new ScopeDisplayEntity();
                newEntity.setScopeId(scopeIdValue);
                newEntity.setTitle(name);
                displays.saveAndFlush(newEntity);
                return;
            }
            entity.setTitle(name);
            displays.saveAndFlush(entity);
        });
    }
}
