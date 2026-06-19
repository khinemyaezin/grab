package com.grab.store.identity.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.identity.internal.command.UserProfileResult;

public record GetUserProfileQuery(Id userId) implements Query<GetUserProfileResult> {
}
