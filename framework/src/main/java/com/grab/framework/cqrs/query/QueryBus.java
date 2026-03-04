package com.grab.framework.cqrs.query;

/**
 * Query Bus interface for dispatching queries to their handlers.
 * Implements the Mediator pattern for query routing.
 */
public interface QueryBus {

    /**
     * Dispatch a query to its appropriate handler.
     *
     * @param query The query to dispatch
     * @param <R>   The return type of the query
     * @return The result of the query execution
     * @throws IllegalArgumentException if no handler is registered for the query type
     */
    <R> R dispatch(Query<R> query);
}
