package com.grab.app.account.internal.config;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AccountBusConfig {

    private static final String GROUP_PACKAGE = "com.grab.app.account";

    @Bean(name = "accountCommandBus")
    public CommandBus accountCommandBus(ObjectProvider<CommandHandler<?, ?>> handlersProvider) {
        return new DefaultCommandBus(() -> handlersProvider.orderedStream()
                .filter(h -> h.getClass().getPackageName().startsWith(GROUP_PACKAGE))
                .toList());
    }

    @Bean(name = "accountQueryBus")
    public QueryBus accountQueryBus(ObjectProvider<QueryHandler<?, ?>> handlersProvider) {
        return new DefaultQueryBus(() -> handlersProvider.orderedStream()
                .filter(h -> h.getClass().getPackageName().startsWith(GROUP_PACKAGE))
                .toList());
    }
}
