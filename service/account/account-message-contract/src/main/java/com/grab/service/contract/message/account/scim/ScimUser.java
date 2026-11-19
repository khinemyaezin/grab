package com.grab.service.contract.message.account.scim;

import java.io.Serializable;
import java.util.List;

public record ScimUser(
        List<String> schemas,
        String id,
        String externalId,
        String userName,
        ScimName name,
        List<ScimEmail> emails,
        boolean active,
        ScimAccessContextExtension accessContexts
) implements Serializable {
    public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:User";

    public ScimUser {
        schemas = schemas == null || schemas.isEmpty() ? List.of(SCHEMA_URI) : List.copyOf(schemas);
        emails = emails == null ? List.of() : List.copyOf(emails);
    }

    public ScimUser(String externalId, String userName, ScimName name, List<ScimEmail> emails, boolean active) {
        this(List.of(SCHEMA_URI), null, externalId, userName, name, emails, active, null);
    }
}
