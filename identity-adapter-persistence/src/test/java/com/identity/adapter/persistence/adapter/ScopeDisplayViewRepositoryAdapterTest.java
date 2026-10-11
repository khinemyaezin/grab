package com.identity.adapter.persistence.adapter;

import com.grab.framework.id.impl.CommonId;
import com.identity.adapter.persistence.entity.ScopeDisplayEntity;
import com.identity.adapter.persistence.repository.jpa.ScopeDisplayJpaRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeDisplayViewRepositoryAdapterTest {
    private ScopeDisplayEntity existing;
    private int flushCount;
    private final ScopeDisplayJpaRepository displays = proxy();
    private final ScopeDisplayViewRepositoryAdapter adapter = new ScopeDisplayViewRepositoryAdapter(
            displays, new IdentityPersistenceExecutor());

    @Test
    void update_missingProjection_insertsSnapshot() {
        adapter.update("merchant.account", new CommonId("merchant-1"), "Shop", null, 4);

        assertThat(existing.getScopeKey()).isEqualTo("merchant.account");
        assertThat(existing.getScopeId()).isEqualTo("merchant-1");
        assertThat(existing.getTitle()).isEqualTo("Shop");
        assertThat(existing.getSubtitle()).isNull();
        assertThat(existing.getSourceRevision()).isEqualTo(4);
        assertThat(flushCount).isEqualTo(1);
    }

    @Test
    void update_staleOrDuplicateRevisionLeavesProjectionUntouched() {
        existing = entity(5, "Current", "Legal");

        adapter.update("merchant.account", new CommonId("merchant-1"), "Older", null, 5);
        adapter.update("merchant.account", new CommonId("merchant-1"), "Stale", null, 4);

        assertThat(existing.getTitle()).isEqualTo("Current");
        assertThat(existing.getSubtitle()).isEqualTo("Legal");
        assertThat(existing.getSourceRevision()).isEqualTo(5);
        assertThat(flushCount).isZero();
    }

    @Test
    void update_newerRevisionReplacesDisplayFields() {
        existing = entity(5, "Old", "Old Legal");

        adapter.update("merchant.account", new CommonId("merchant-1"), "New", null, 6);

        assertThat(existing.getTitle()).isEqualTo("New");
        assertThat(existing.getSubtitle()).isNull();
        assertThat(existing.getSourceRevision()).isEqualTo(6);
        assertThat(flushCount).isEqualTo(1);
    }

    private ScopeDisplayEntity entity(long revision, String title, String subtitle) {
        ScopeDisplayEntity entity = new ScopeDisplayEntity();
        entity.setScopeKey("merchant.account");
        entity.setScopeId("merchant-1");
        entity.setTitle(title);
        entity.setSubtitle(subtitle);
        entity.setSourceRevision(revision);
        return entity;
    }

    private ScopeDisplayJpaRepository proxy() {
        Object repository = Proxy.newProxyInstance(ScopeDisplayJpaRepository.class.getClassLoader(),
                new Class<?>[]{ScopeDisplayJpaRepository.class}, (instance, method, arguments) -> {
                    if (method.getName().equals("findByScopeKeyAndScopeId")) {
                        return Optional.ofNullable(existing);
                    }
                    if (method.getName().equals("saveAndFlush")) {
                        existing = (ScopeDisplayEntity) arguments[0];
                        flushCount++;
                        return existing;
                    }
                    return null;
                });
        return ScopeDisplayJpaRepository.class.cast(repository);
    }
}
