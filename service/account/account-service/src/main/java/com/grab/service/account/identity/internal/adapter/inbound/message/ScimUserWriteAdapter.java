package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.service.contract.message.account.scim.ScimEmail;
import com.grab.service.contract.message.account.scim.ScimName;
import com.grab.service.contract.message.account.scim.ScimUser;
import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.ProvisionExternalUserCommand;
import com.identity.application.model.write.UpdateExternalUserStatusCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScimUserWriteAdapter {
    private final CommandBus commands;

    public ScimUserWriteAdapter(@Qualifier("accountCommandBus") CommandBus commands) {
        this.commands = commands;
    }

    public ScimUser provisionUser(ScimUser request) {
        String email = request.userName();
        if ((email == null || email.isBlank()) && request.emails() != null && !request.emails().isEmpty()) {
            email = request.emails().getFirst().value();
        }
        String givenName = request.name() != null ? request.name().givenName() : null;
        String familyName = request.name() != null ? request.name().familyName() : null;

        ProvisionExternalUserCommand command = new ProvisionExternalUserCommand(request.externalId(), email, givenName, familyName);
        ExternalUserProvisionResult result = commands.dispatch(command);

        return new ScimUser(
                List.of(ScimUser.SCHEMA_URI),
                result.userId().getValue(),
                result.externalId(),
                result.email(),
                new ScimName(givenName, familyName),
                List.of(new ScimEmail(result.email(), "work", true)),
                "ACTIVE".equalsIgnoreCase(result.status()),
                null
        );
    }

    public ScimUser updateUserStatus(String idOrExternalId, boolean active) {
        UpdateExternalUserStatusCommand command = new UpdateExternalUserStatusCommand(idOrExternalId, active);
        ExternalUserProvisionResult result = commands.dispatch(command);

        return new ScimUser(
                List.of(ScimUser.SCHEMA_URI),
                result.userId().getValue(),
                result.externalId(),
                result.email(),
                new ScimName(null, null),
                List.of(new ScimEmail(result.email(), "work", true)),
                "ACTIVE".equalsIgnoreCase(result.status()),
                null
        );
    }
}
