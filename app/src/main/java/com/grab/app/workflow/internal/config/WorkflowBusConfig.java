package com.grab.app.workflow.internal.config;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WorkflowBusConfig {

    private static final String GROUP_PACKAGE = "com.grab.app.workflow";

    @Bean(name = "workflowCommandBus")
    public CommandBus workflowCommandBus(ObjectProvider<CommandHandler<?, ?>> handlersProvider) {
        return new DefaultCommandBus(() -> handlersProvider.orderedStream()
                .filter(WorkflowBusConfig::ownsHandler)
                .toList());
    }

    @Bean(name = "workflowQueryBus")
    public QueryBus workflowQueryBus(ObjectProvider<QueryHandler<?, ?>> handlersProvider) {
        return new DefaultQueryBus(() -> handlersProvider.orderedStream()
                .filter(WorkflowBusConfig::ownsHandler)
                .toList());
    }
    private static boolean ownsHandler(Object handler) {
        Class<?> target = AopUtils.getTargetClass(handler);
        String packageName = target.getPackageName();
        return packageName.equals(GROUP_PACKAGE) || packageName.startsWith(GROUP_PACKAGE + ".");
    }
}
