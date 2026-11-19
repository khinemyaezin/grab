package com.identity.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetExternalUserQuery(String idOrExternalId) implements Query<ExternalUserView> {
}
