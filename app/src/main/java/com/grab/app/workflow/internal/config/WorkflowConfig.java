package com.grab.app.workflow.internal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        WorkflowConfiguration.class,
        WorkflowDataSourceConfig.class,
        WorkflowBusConfig.class
})
public class WorkflowConfig {
}

