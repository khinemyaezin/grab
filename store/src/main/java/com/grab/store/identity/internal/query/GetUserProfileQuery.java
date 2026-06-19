package com.grab.store.identity.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.identity.internal.command.UserProfileResult;

public record GetUserProfileQuery(String userId) implements Query<UserProfileResult> {}
