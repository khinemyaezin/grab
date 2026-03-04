package com.grab.store.shared.config;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CqrsConfiguration {

    @Bean
    public CommandBus commandBus(List<CommandHandler<?, ?>> commandHandlers) {
        return new DefaultCommandBus(commandHandlers);
    }

    @Bean
    public QueryBus queryBus(List<QueryHandler<?, ?>> queryHandlers) {
        return new DefaultQueryBus(queryHandlers);
    }
}
