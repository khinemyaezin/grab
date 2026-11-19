package com.grab.service.contract.message.account.scim;

import java.io.Serializable;

public record ScimName(String givenName, String familyName) implements Serializable {}
