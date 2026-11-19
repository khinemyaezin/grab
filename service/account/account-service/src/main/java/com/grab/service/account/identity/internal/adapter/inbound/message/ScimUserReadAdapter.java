package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.service.contract.message.account.scim.ScimAccessContext;
import com.grab.service.contract.message.account.scim.ScimAccessContextExtension;
import com.grab.service.contract.message.account.scim.ScimEmail;
import com.grab.service.contract.message.account.scim.ScimName;
import com.grab.service.contract.message.account.scim.ScimUser;
import com.identity.application.model.read.ExternalUserView;
import com.identity.application.model.read.GetExternalUserQuery;
import com.identity.application.model.read.GetUserAccessContextsQuery;
import com.identity.application.model.read.UserAccessContextsResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScimUserReadAdapter {
    private final QueryBus queries;

    public ScimUserReadAdapter(@Qualifier("accountQueryBus") QueryBus queries) {
        this.queries = queries;
    }

    public ScimUser getUser(String idOrExternalId) {
        if (idOrExternalId == null || idOrExternalId.isBlank()) {
            return null;
        }
        ExternalUserView userView = queries.dispatch(new GetExternalUserQuery(idOrExternalId));
        if (userView == null) {
            return null;
        }

        UserAccessContextsResult contextsResult = queries.dispatch(new GetUserAccessContextsQuery(userView.userId()));
        List<ScimAccessContext> contexts = contextsResult != null && contextsResult.contexts() != null
                ? contextsResult.contexts().stream()
                .map(c -> new ScimAccessContext(c.assignmentId(), c.scopeKey(), c.scopeId(), c.role(), c.displayName(), c.authorities()))
                .toList()
                : List.of();
        ScimAccessContextExtension extension = new ScimAccessContextExtension(contexts);

        return new ScimUser(
                List.of(ScimUser.SCHEMA_URI, ScimAccessContextExtension.SCHEMA_URI),
                userView.userId().getValue(),
                userView.externalId(),
                userView.email(),
                new ScimName(null, null),
                List.of(new ScimEmail(userView.email(), "work", true)),
                "ACTIVE".equalsIgnoreCase(userView.status()),
                extension
        );
    }

    public ScimAccessContextExtension getUserAccessContexts(String idOrExternalId) {
        if (idOrExternalId == null || idOrExternalId.isBlank()) {
            return new ScimAccessContextExtension(List.of());
        }
        ExternalUserView userView = queries.dispatch(new GetExternalUserQuery(idOrExternalId));
        if (userView == null) {
            return new ScimAccessContextExtension(List.of());
        }

        UserAccessContextsResult contextsResult = queries.dispatch(new GetUserAccessContextsQuery(userView.userId()));
        List<ScimAccessContext> contexts = contextsResult != null && contextsResult.contexts() != null
                ? contextsResult.contexts().stream()
                .map(c -> new ScimAccessContext(c.assignmentId(), c.scopeKey(), c.scopeId(), c.role(), c.displayName(), c.authorities()))
                .toList()
                : List.of();

        return new ScimAccessContextExtension(contexts);
    }
}
