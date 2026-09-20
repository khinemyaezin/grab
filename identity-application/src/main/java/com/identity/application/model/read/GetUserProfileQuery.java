package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.identity.application.model.write.UserProfileResult;

public record GetUserProfileQuery(Id userId) implements Query<GetUserProfileResult> {
}
