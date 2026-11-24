package com.grab.service.contract.message.account.scim;

import java.io.Serializable;
import java.util.List;

public record ScimError(
        List<String> schemas,
        String status,
        String scimType,
        String detail
) implements Serializable {
    public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Error";

    public ScimError(String status, String scimType, String detail) {
        this(List.of(SCHEMA_URI), status, scimType, detail);
    }
}
