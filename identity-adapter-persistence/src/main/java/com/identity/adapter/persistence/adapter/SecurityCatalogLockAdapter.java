package com.identity.adapter.persistence.adapter;

import com.identity.adapter.persistence.entity.SecurityCatalogStateEntity;
import com.identity.adapter.persistence.repository.jpa.SecurityCatalogStateJpaRepository;
import com.identity.domain.port.outbound.SecurityCatalogLock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityCatalogLockAdapter implements SecurityCatalogLock {
    private final SecurityCatalogStateJpaRepository repository;

    @Override
    public void lockNowait() {
        SecurityCatalogStateEntity state = repository.lockSingleton();
        if (state == null) {
            state = new SecurityCatalogStateEntity();
            state.setId(1L);
            state.setCatalogRevision(0L);
            repository.saveAndFlush(state);
            repository.lockSingleton();
        }
    }

    @Override
    public void recordActivation() {
        var state = repository.lockSingleton();
        state.setCatalogRevision(state.getCatalogRevision() + 1);
        repository.save(state);
    }
}
