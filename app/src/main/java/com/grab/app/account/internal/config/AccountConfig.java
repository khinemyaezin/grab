package com.grab.app.account.internal.config;

import com.grab.app.account.customer.internal.config.CustomerConfig;
import com.grab.app.account.identity.internal.config.IdentityConfig;
import com.grab.app.account.merchant.internal.config.MerchantConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        AccountDataSourceConfig.class,
        AccountBusConfig.class,
        IdentityConfig.class,
        CustomerConfig.class,
        MerchantConfig.class
})
public class AccountConfig {
}

