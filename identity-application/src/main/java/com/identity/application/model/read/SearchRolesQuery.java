package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record SearchRolesQuery(String name) implements Query<SearchRolesResult> {
}
