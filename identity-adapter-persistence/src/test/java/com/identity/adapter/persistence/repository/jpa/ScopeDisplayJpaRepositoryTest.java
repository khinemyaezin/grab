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
        displays.saveAndFlush(display("merchant.account", "same-id", "Merchant", null, 3));
        displays.saveAndFlush(display("merchant.storefront", "same-id", "Store", "store-slug", 8));

        List<ScopeDisplayView> result = displays.findDisplays(
                List.of("merchant.account", "merchant.storefront"), List.of("same-id"));

        assertThat(result).containsExactlyInAnyOrder(
                new ScopeDisplayView("merchant.account", "same-id", "Merchant", null),
                new ScopeDisplayView("merchant.storefront", "same-id", "Store", "store-slug"));
    }

    private ScopeDisplayEntity display(String scopeKey, String scopeId, String title, String subtitle, long revision) {
        ScopeDisplayEntity entity = new ScopeDisplayEntity();
        entity.setScopeKey(scopeKey);
        entity.setScopeId(scopeId);
        entity.setTitle(title);
        entity.setSubtitle(subtitle);
        entity.setSourceRevision(revision);
        return entity;
    }
}
