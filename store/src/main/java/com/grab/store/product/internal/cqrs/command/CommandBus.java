package com.grab.store.product.internal.cqrs.command;

/**
 * Command Bus interface for dispatching commands to their handlers.
 * Implements the Mediator pattern for command routing.
 */
public interface CommandBus {

    /**
     * Dispatch a command to its appropriate handler.
     *
     * @param command The command to dispatch
     * @param <R>     The return type of the command
     * @return The result of the command execution
     * @throws IllegalArgumentException if no handler is registered for the command type
     */
    <R> R dispatch(Command<R> command);
}
