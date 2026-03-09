package com.grab.framework.cqrs.command.impl;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.exception.FrameworkException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultCommandBusTest {

    @Test
    void dispatch_withoutRegisteredHandler_shouldThrowFrameworkException() {
        DefaultCommandBus bus = new DefaultCommandBus(List.of());

        FrameworkException exception = assertThrows(
                FrameworkException.class,
                () -> bus.dispatch(new MissingCommand())
        );

        assertEquals("shr.internal.cqrs_handler_missing", exception.getMessageSource().code());
        assertEquals("command", exception.getMessageSource().args().get("type"));
    }

    @Test
    void dispatch_withRegisteredHandler_shouldReturnResult() {
        DefaultCommandBus bus = new DefaultCommandBus(List.of(new EchoCommandHandler()));

        String result = bus.dispatch(new EchoCommand("ok"));

        assertEquals("ok", result);
    }

    private record MissingCommand() implements Command<String> {
    }

    private record EchoCommand(String payload) implements Command<String> {
    }

    private static final class EchoCommandHandler implements CommandHandler<EchoCommand, String> {
        @Override
        public String handle(EchoCommand command) {
            return command.payload();
        }

        @Override
        public Class<EchoCommand> getCommandType() {
            return EchoCommand.class;
        }
    }
}
