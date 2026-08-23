package com.grab.store.shared.tracing;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.task.TaskDecorator;

@Configuration
public class TracingConfiguration {

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>(new TraceIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public TaskDecorator mdcCopyingTaskDecorator() {
        return new MdcCopyingTaskDecorator();
    }

    @Bean
    public ScheduledTraceContextAspect scheduledTraceContextAspect() {
        return new ScheduledTraceContextAspect();
    }
}
