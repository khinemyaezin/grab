package com.grab.store.product.internal.cqrs.command;

/**
 * Handler interface for processing commands.
 * Each command type should have exactly one handler.
 *
 * @param <C> The command type this handler processes
 * @param <R> The return type of the command execution
 */
public interface CommandHandler<C extends Command<R>, R> {

    /**
     * Handle the given command and return the result.
     *
     * @param command The command to process
     * @return The result of command execution
     */
    R handle(C command);

    /**
     * Returns the command class this handler processes.
     * Used by the CommandBus for routing.
     */
    Class<C> getCommandType();
}
