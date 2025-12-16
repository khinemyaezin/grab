package com.grab.store.product.internal.cqrs.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.grab.store.product.internal.cqrs",
        "com.grab.store.product.internal.command.handler",
        "com.grab.store.product.internal.query.handler"
})
public class CqrsConfiguration {

}
