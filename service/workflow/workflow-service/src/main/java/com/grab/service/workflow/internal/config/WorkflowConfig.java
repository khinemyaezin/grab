package com.grab.service.workflow.internal.config;

import com.grab.service.workflow.internal.config.tracing.WorkflowTracingConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        CentralMapperConfig.class,
        ServiceSharedConfiguration.class,
        WorkflowConfiguration.class,
        WorkflowDataSourceConfig.class,
        WorkflowBusConfig.class,
        WorkflowTracingConfiguration.class
})
public class WorkflowConfig {
}
