package com.grab.store.cart.internal.config;

import com.cart.adapter.persistence.config.CartPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CartUseCaseConfig.class, CartPersistenceConfig.class})
public class CartConfig {
}
