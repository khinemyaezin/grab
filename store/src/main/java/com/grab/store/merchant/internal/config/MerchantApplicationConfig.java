package com.grab.store.merchant.internal.config;

import com.merchant.domain.service.MerchantApprovalPolicy;
import com.merchant.domain.service.SystemDefaultMerchantApprovalPolicy;
import com.merchant.domain.service.MerchantRegistrationPolicy;
import com.merchant.domain.repository.MerchantAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MerchantEnabled
public class MerchantApplicationConfig {

    @Bean
    MerchantRegistrationPolicy merchantRegistrationPolicy(MerchantAccountRepository merchants) {
        return new MerchantRegistrationPolicy(merchants);
    }

    @Bean
    MerchantApprovalPolicy merchantApprovalPolicy() {
        return new SystemDefaultMerchantApprovalPolicy();
    }
}
