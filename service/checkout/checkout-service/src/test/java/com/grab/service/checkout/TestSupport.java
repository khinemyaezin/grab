package com.grab.service.checkout;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;

public final class TestSupport {

    private TestSupport() {
    }

    public static <T> T mapper(Class<T> mapperType) {
        T mapper = Mappers.getMapper(mapperType);
        IdGenerator idGenerator = new UuidGenerator();
        IdMapper idMapper = new IdMapper(idGenerator);
        Class<?> current = mapper.getClass();
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getType().equals(IdMapper.class)) {
                    try {
                        field.setAccessible(true);
                        field.set(mapper, idMapper);
                    } catch (IllegalAccessException exception) {
                        throw new IllegalStateException(exception);
                    }
                }
            }
            current = current.getSuperclass();
        }
        return mapper;
    }

    public static final class RecordingCommandBus implements CommandBus {
        private Command<?> dispatchedCommand;
        private Object result;

        @Override
        @SuppressWarnings("unchecked")
        public <R> R dispatch(Command<R> command) {
            dispatchedCommand = command;
            return (R) result;
        }

        public Command<?> dispatchedCommand() {
            return dispatchedCommand;
        }
    }

    public static final class RecordingQueryBus implements QueryBus {
        private Query<?> dispatchedQuery;
        private Object result;

        @Override
        @SuppressWarnings("unchecked")
        public <R> R dispatch(Query<R> query) {
            dispatchedQuery = query;
            return (R) result;
        }

        public Query<?> dispatchedQuery() {
            return dispatchedQuery;
        }
    }
}
