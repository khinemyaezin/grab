package com.grab.service.contract.message.account.scim;

import java.io.Serializable;
import java.util.List;

public record ScimAccessContextExtension(
        List<ScimAccessContext> contexts
) implements Serializable {
    public static final String SCHEMA_URI = "urn:grab:params:scim:schemas:extension:AccessContexts";

    public ScimAccessContextExtension {
        contexts = contexts == null ? List.of() : List.copyOf(contexts);
    }
}
