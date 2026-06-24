package com.grab.store.identity.internal.query;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record SearchRolesQuery(String name) implements Query<SearchRolesResult> {
}
