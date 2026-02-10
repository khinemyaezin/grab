package com.grab.store.catalog.internal.cqrs.query;

/**
 * Handler interface for processing queries.
 * Each query type should have exactly one handler.
 *
 * @param <Q> The query type this handler processes
 * @param <R> The return type of the query execution
 */
public interface QueryHandler<Q extends Query<R>, R> {

    /**
     * Handle the given query and return the result.
     *
     * @param query The query to process
     * @return The result of query execution
     */
    R handle(Q query);

    /**
     * Returns the query class this handler processes.
     * Used by the QueryBus for routing.
     */
    Class<Q> getQueryType();
}
