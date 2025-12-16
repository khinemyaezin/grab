package com.grab.store.product.internal.cqrs.command.impl;

import com.grab.store.product.internal.cqrs.command.Command;
import com.grab.store.product.internal.cqrs.command.CommandBus;
import com.grab.store.product.internal.cqrs.command.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SimpleCommandBus implements CommandBus {

    private final Map<Class<?>, CommandHandler<?, ?>> handlers = new HashMap<>();

    public SimpleCommandBus(List<CommandHandler<?, ?>> commandHandlers) {
        commandHandlers.forEach(handler -> {
            Class<?> commandType = handler.getCommandType();
            if (handlers.containsKey(commandType)) {
                throw new IllegalStateException(
                        "Duplicate handler for command type: " + commandType.getName());
            }
            handlers.put(commandType, handler);
            log.info("Registered command handler: {} for command: {}",
                    handler.getClass().getSimpleName(), commandType.getSimpleName());
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(Command<R> command) {
        CommandHandler<Command<R>, R> handler =
                (CommandHandler<Command<R>, R>) handlers.get(command.getClass());

        if (handler == null) {
            throw new IllegalArgumentException(
                    "No handler registered for command: " + command.getClass().getName());
        }

        log.debug("Dispatching command: {} to handler: {}",
                command.getClass().getSimpleName(), handler.getClass().getSimpleName());

        return handler.handle(command);
    }
}
