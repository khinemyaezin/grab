package com.grab.store.merchant.internal.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantEnabledTest {
    @Test
    void merchantEnabled_withMissingProperty_shouldRemainDisabled() {
        ConditionalOnProperty applicationCondition = MerchantEnabled.class
                .getAnnotation(ConditionalOnProperty.class);
        ConditionalOnProperty datasourceCondition = MerchantModuleDataSourceConfig.class
                .getAnnotation(ConditionalOnProperty.class);

        assertThat(applicationCondition.matchIfMissing()).isFalse();
        assertThat(datasourceCondition.matchIfMissing()).isFalse();
    }
}
