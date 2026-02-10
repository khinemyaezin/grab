package com.grab.store.catalog.internal.cqrs.command;

/**
 * Base marker interface for all commands.
 * Commands represent write operations that change state.
 *
 * @param <R> The return type of the command execution
 */
public interface Command<R> {
    // Marker interface - commands declare their return type
}
