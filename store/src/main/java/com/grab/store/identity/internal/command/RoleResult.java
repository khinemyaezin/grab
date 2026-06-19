package com.grab.store.identity.internal.command;

import java.util.Set;

public record RoleResult(String code, String name, String description, boolean active, Set<String> authorities) {}
