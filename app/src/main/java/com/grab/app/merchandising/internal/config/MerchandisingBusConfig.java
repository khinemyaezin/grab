package com.grab.app.merchandising.internal.config;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class MerchandisingBusConfig {

    private static final String GROUP_PACKAGE = "com.grab.app.merchandising";

    @Bean(name = "merchandisingCommandBus")
    public CommandBus merchandisingCommandBus(
            @Autowired(required = false) List<CommandHandler<?, ?>> handlers) {
        List<CommandHandler<?, ?>> groupHandlers = handlers == null
                ? Collections.emptyList()
                : handlers.stream()
                .filter(h -> h.getClass().getPackageName().startsWith(GROUP_PACKAGE))
                .toList();
        return new DefaultCommandBus(groupHandlers);
    }

    @Bean(name = "merchandisingQueryBus")
    public QueryBus merchandisingQueryBus(
            @Autowired(required = false) List<QueryHandler<?, ?>> handlers) {
        List<QueryHandler<?, ?>> groupHandlers = handlers == null
                ? Collections.emptyList()
                : handlers.stream()
                .filter(h -> h.getClass().getPackageName().startsWith(GROUP_PACKAGE))
                .toList();
        return new DefaultQueryBus(groupHandlers);
    }
}

