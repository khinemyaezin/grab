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
        adapter.update(new CommonId("merchant-1"), "Shop");

        assertThat(existing.getScopeId()).isEqualTo("merchant-1");
        assertThat(existing.getTitle()).isEqualTo("Shop");
        assertThat(flushCount).isEqualTo(1);
    }

    @Test
    void update_existingProjection_updatesTitle() {
        existing = entity("Current");

        adapter.update(new CommonId("merchant-1"), "New");

        assertThat(existing.getTitle()).isEqualTo("New");
        assertThat(flushCount).isEqualTo(1);
    }

    private ScopeDisplayEntity entity(String title) {
        ScopeDisplayEntity entity = new ScopeDisplayEntity();
        entity.setScopeId("merchant-1");
        entity.setTitle(title);
        return entity;
    }

    private ScopeDisplayJpaRepository proxy() {
        Object repository = Proxy.newProxyInstance(ScopeDisplayJpaRepository.class.getClassLoader(),
                new Class<?>[]{ScopeDisplayJpaRepository.class}, (instance, method, arguments) -> {
                    if (method.getName().equals("findByScopeId")) {
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
