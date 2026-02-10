package com.grab.store.catalog.internal.cqrs.query;

/**
 * Base marker interface for all queries.
 * Queries represent read operations that do not change state.
 *
 * @param <R> The return type of the query execution
 */
public interface Query<R> {
    // Marker interface - queries declare their return type
}
