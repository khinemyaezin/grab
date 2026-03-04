package com.grab.framework.cqrs.query.impl;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.QueryHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DefaultQueryBus implements QueryBus {

    private final Map<Class<?>, QueryHandler<?, ?>> handlers = new HashMap<>();

    public DefaultQueryBus(List<QueryHandler<?, ?>> queryHandlers) {
        queryHandlers.forEach(handler -> {
            Class<?> queryType = handler.getQueryType();
            if (handlers.containsKey(queryType)) {
                throw new IllegalStateException(
                        "Duplicate handler for query type: " + queryType.getName());
            }
            handlers.put(queryType, handler);
//            log.info("Registered query handler: {} for query: {}",
//                    handler.getClass().getSimpleName(), queryType.getSimpleName());
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

//        log.debug("Dispatching query: {} to handler: {}",
//                query.getClass().getSimpleName(), handler.getClass().getSimpleName());

        return handler.handle(query);
    }
}
