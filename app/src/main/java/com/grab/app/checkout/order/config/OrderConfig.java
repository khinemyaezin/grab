package com.grab.app.checkout.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        OrderModuleDataSourceConfig.class
})
public class OrderConfig {
}

