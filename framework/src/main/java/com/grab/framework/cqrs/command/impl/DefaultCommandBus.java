package com.grab.framework.cqrs.command.impl;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.exception.FrameworkError;
import com.grab.framework.exception.FrameworkException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCommandBus implements CommandBus {
    private final Map<Class<?>, CommandHandler<?, ?>> handlers = new HashMap<>();

    public DefaultCommandBus(List<CommandHandler<?, ?>> commandHandlers) {
        commandHandlers.forEach(handler -> {
            Class<?> commandType = handler.getCommandType();
            if (handlers.containsKey(commandType)) {
                throw new IllegalStateException(
                        "Duplicate handler for command type: " + commandType.getName());
            }
            handlers.put(commandType, handler);
//            log.info("Registered command handler: {} for command: {}",
//                    handler.getClass().getSimpleName(), commandType.getSimpleName());
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(Command<R> command) {
        CommandHandler<Command<R>, R> handler =
                (CommandHandler<Command<R>, R>) handlers.get(command.getClass());

        if (handler == null) {
            throw new FrameworkException(
                    new FrameworkError.CqrsHandlerMissing("command", command.getClass().getName()),
                    "No handler registered for command: " + command.getClass().getName()
            );
        }

//        log.debug("Dispatching command: {} to handler: {}",
//                command.getClass().getSimpleName(), handler.getClass().getSimpleName());

        return handler.handle(command);
    }
}
