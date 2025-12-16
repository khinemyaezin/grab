package com.grab.store.product.internal.cqrs.query.impl;

import com.grab.store.product.internal.cqrs.query.Query;
import com.grab.store.product.internal.cqrs.query.QueryBus;
import com.grab.store.product.internal.cqrs.query.QueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SimpleQueryBus implements QueryBus {

    private final Map<Class<?>, QueryHandler<?, ?>> handlers = new HashMap<>();

    public SimpleQueryBus(List<QueryHandler<?, ?>> queryHandlers) {
        queryHandlers.forEach(handler -> {
            Class<?> queryType = handler.getQueryType();
            if (handlers.containsKey(queryType)) {
                throw new IllegalStateException(
                        "Duplicate handler for query type: " + queryType.getName());
            }
            handlers.put(queryType, handler);
            log.info("Registered query handler: {} for query: {}",
                    handler.getClass().getSimpleName(), queryType.getSimpleName());
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(Query<R> query) {
        QueryHandler<Query<R>, R> handler =
                (QueryHandler<Query<R>, R>) handlers.get(query.getClass());

        if (handler == null) {
            throw new IllegalArgumentException(
                    "No handler registered for query: " + query.getClass().getName());
        }

        log.debug("Dispatching query: {} to handler: {}",
                query.getClass().getSimpleName(), handler.getClass().getSimpleName());

        return handler.handle(query);
    }
}
