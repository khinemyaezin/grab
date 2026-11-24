package com.grab.service.authorizationserver.config.tracing;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.task.TaskDecorator;

@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
public class TracingConfiguration {

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        TraceIdFilter filter = new TraceIdFilter();
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>(filter);
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
