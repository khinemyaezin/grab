package com.grab.store.identity.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.identity.internal.command.RoleResult;
import java.util.List;

public record ListRolesQuery() implements Query<List<RoleResult>> {}
