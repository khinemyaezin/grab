package com.grab.service.contract.message.account.scim;

import java.io.Serializable;

public record ScimEmail(String value, String type, boolean primary) implements Serializable {
    public ScimEmail(String value) {
        this(value, "work", true);
    }
}
