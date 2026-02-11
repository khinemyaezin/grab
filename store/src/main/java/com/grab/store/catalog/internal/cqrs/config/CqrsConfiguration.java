package com.grab.store.catalog.internal.cqrs.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.grab.store.catalog.internal.cqrs",
        "com.grab.store.catalog.internal.command.handler",
        "com.grab.store.catalog.internal.query.handler"
})
public class CqrsConfiguration {
}
