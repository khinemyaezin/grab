package com.grab.framework.cqrs.command.impl;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.exception.FrameworkError;
import com.grab.framework.exception.FrameworkException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultCommandBus implements CommandBus {
    private final Supplier<List<CommandHandler<?, ?>>> handlersSupplier;
    private volatile Map<Class<?>, CommandHandler<?, ?>> handlers;

    public DefaultCommandBus(List<CommandHandler<?, ?>> commandHandlers) {
        this(() -> commandHandlers);
    }

    public DefaultCommandBus(Supplier<List<CommandHandler<?, ?>>> handlersSupplier) {
        this.handlersSupplier = handlersSupplier != null ? handlersSupplier : List::of;
    }

    private Map<Class<?>, CommandHandler<?, ?>> getHandlers() {
        Map<Class<?>, CommandHandler<?, ?>> result = handlers;
        if (result == null) {
            synchronized (this) {
                result = handlers;
                if (result == null) {
                    result = new HashMap<>();
                    List<CommandHandler<?, ?>> commandHandlers = handlersSupplier.get();
                    if (commandHandlers != null) {
                        for (CommandHandler<?, ?> handler : commandHandlers) {
                            Class<?> commandType = handler.getCommandType();
                            if (result.containsKey(commandType)) {
                                throw new IllegalStateException(
                                        "Duplicate handler for command type: " + commandType.getName());
                            }
                            result.put(commandType, handler);
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
    public <R> R dispatch(Command<R> command) {
        CommandHandler<Command<R>, R> handler =
                (CommandHandler<Command<R>, R>) getHandlers().get(command.getClass());

        if (handler == null) {
            throw new FrameworkException(
                    new FrameworkError.CqrsHandlerMissing("command", command.getClass().getName()),
                    "No handler registered for command: " + command.getClass().getName()
            );
        }

        return handler.handle(command);
    }
}
