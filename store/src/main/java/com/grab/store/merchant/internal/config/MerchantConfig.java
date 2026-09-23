package com.grab.store.merchant.internal.config;

import com.merchant.adapter.persistence.config.MerchantPersistenceConfig;
import com.merchant.domain.policy.impl.DefaultMerchantApprovalAccessPolicy;
import com.merchant.domain.policy.MerchantApprovalAccessPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@MerchantEnabled
@Import({MerchantUseCaseConfig.class, MerchantPersistenceConfig.class})
public class MerchantConfig {
    @Bean
    public MerchantApprovalAccessPolicy merchantApprovalAccessPolicy() {
        return new DefaultMerchantApprovalAccessPolicy();
    }
}
