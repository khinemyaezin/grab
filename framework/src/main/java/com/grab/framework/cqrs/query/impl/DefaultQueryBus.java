package com.grab.framework.cqrs.query.impl;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.exception.FrameworkError;
import com.grab.framework.exception.FrameworkException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultQueryBus implements QueryBus {

    private final Supplier<List<QueryHandler<?, ?>>> handlersSupplier;
    private volatile Map<Class<?>, QueryHandler<?, ?>> handlers;

    public DefaultQueryBus(List<QueryHandler<?, ?>> queryHandlers) {
        this(() -> queryHandlers);
    }

    public DefaultQueryBus(Supplier<List<QueryHandler<?, ?>>> handlersSupplier) {
        this.handlersSupplier = handlersSupplier != null ? handlersSupplier : List::of;
    }

    private Map<Class<?>, QueryHandler<?, ?>> getHandlers() {
        Map<Class<?>, QueryHandler<?, ?>> result = handlers;
        if (result == null) {
            synchronized (this) {
                result = handlers;
                if (result == null) {
                    result = new HashMap<>();
                    List<QueryHandler<?, ?>> queryHandlers = handlersSupplier.get();
                    if (queryHandlers != null) {
                        for (QueryHandler<?, ?> handler : queryHandlers) {
                            Class<?> queryType = handler.getQueryType();
                            if (result.containsKey(queryType)) {
                                throw new IllegalStateException(
                                        "Duplicate handler for query type: " + queryType.getName());
                            }
                            result.put(queryType, handler);
                        }
                    }
                    handlers = result;
                }
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(Query<R> query) {
        QueryHandler<Query<R>, R> handler =
                (QueryHandler<Query<R>, R>) getHandlers().get(query.getClass());

        if (handler == null) {
            throw new FrameworkException(
                    new FrameworkError.CqrsHandlerMissing("query", query.getClass().getName()),
                    "No handler registered for query: " + query.getClass().getName()
            );
        }

        return handler.handle(query);
    }
}
