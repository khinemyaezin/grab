package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.ScopeDisplayEntity;
import com.identity.application.model.read.ScopeDisplayView;
import com.identity.adapter.persistence.repository.jpa.config.RepositoryTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeDisplayJpaRepositoryTest extends RepositoryTestConfig {
    @Autowired
    private ScopeDisplayJpaRepository displays;

    @Test
    void findDisplays_bulkLoadsSnapshotsWithNullableSubtitle() {
        displays.saveAndFlush(display("merchant-1", "Merchant", null));
        displays.saveAndFlush(display("storefront-1", "Store", "store-slug"));

        List<ScopeDisplayView> result = displays.findDisplays(
                List.of("merchant-1", "storefront-1"));

        assertThat(result).containsExactlyInAnyOrder(
                new ScopeDisplayView("merchant-1", "Merchant", null),
                new ScopeDisplayView("storefront-1", "Store", "store-slug"));
    }

    @Test
    void findByScopeId_returnsMatchingEntity() {
        displays.saveAndFlush(display("merchant-2", "Another Merchant", null));

        assertThat(displays.findByScopeId("merchant-2")).isPresent()
                .hasValueSatisfying(entity -> assertThat(entity.getTitle()).isEqualTo("Another Merchant"));
    }

    private ScopeDisplayEntity display(String scopeId, String title, String subtitle) {
        ScopeDisplayEntity entity = new ScopeDisplayEntity();
        entity.setScopeId(scopeId);
        entity.setTitle(title);
        entity.setSubtitle(subtitle);
        return entity;
    }
}
