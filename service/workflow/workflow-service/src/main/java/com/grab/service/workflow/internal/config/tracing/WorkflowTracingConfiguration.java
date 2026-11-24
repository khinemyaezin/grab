package com.grab.service.workflow.internal.config.tracing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
public class WorkflowTracingConfiguration {

    @Bean
    public ScheduledTraceContextAspect scheduledTraceContextAspect() {
        return new ScheduledTraceContextAspect();
    }
}
