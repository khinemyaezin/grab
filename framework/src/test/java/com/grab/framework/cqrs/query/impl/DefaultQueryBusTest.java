package com.grab.framework.cqrs.query.impl;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.exception.FrameworkException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultQueryBusTest {

    @Test
    void dispatch_withoutRegisteredHandler_shouldThrowFrameworkException() {
        DefaultQueryBus bus = new DefaultQueryBus(List.of());

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> bus.dispatch(new MissingQuery())
        );

        assertEquals("shr.internal.cqrs_handler_missing", exception.getMessageSource().code());
        assertEquals("query", exception.getMessageSource().args().get("type"));
    }

    @Test
    void dispatch_withRegisteredHandler_shouldReturnResult() {
        DefaultQueryBus bus = new DefaultQueryBus(List.of(new EchoQueryHandler()));

        String result = bus.dispatch(new EchoQuery("ok"));

        assertEquals("ok", result);
    }

    private record MissingQuery() implements Query<String> {
    }

    private record EchoQuery(String payload) implements Query<String> {
    }

    private static final class EchoQueryHandler implements QueryHandler<EchoQuery, String> {
        @Override
        public String handle(EchoQuery query) {
            return query.payload();
        }

        @Override
        public Class<EchoQuery> getQueryType() {
            return EchoQuery.class;
        }
    }
}
