package com.grab.store.identity.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.identity.internal.command.UserProfileResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record ListUsersQuery(Pageable pageable) implements Query<Page<UserProfileResult>> {}
