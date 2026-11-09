package com.grab.app.checkout.cart.internal.config;

import com.cart.adapter.persistence.config.CartPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        CartUseCaseConfig.class,
        CartPersistenceConfig.class,
        CartModuleDataSourceConfig.class,
        CartModuleOutboxConfig.class
})
public class CartConfig {
}

