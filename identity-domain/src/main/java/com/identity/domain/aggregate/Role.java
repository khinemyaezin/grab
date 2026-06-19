package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class Role extends AggregateRoot<Id> {
    private final String code;
    private String name;
    private String description;
    private boolean active;
    private final Set<String> authorityCodes;

    public Role(Id id, String code, String name, String description, boolean active, Set<String> authorityCodes) {
        super(id);
        this.code = normalize(code);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.active = active;
        this.authorityCodes = new LinkedHashSet<>(authorityCodes);
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value).trim().toUpperCase();
        if (!normalized.matches("[A-Z][A-Z0-9_]*")) throw new IllegalArgumentException("invalid role code");
        return normalized;
    }

    public void updateDetails(String name, String description) {
        this.name = Objects.requireNonNull(name);
        this.description = description;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public void assignAuthority(String code) {
        authorityCodes.add(normalize(code));
    }

    public void revokeAuthority(String code) {
        authorityCodes.remove(normalize(code));
    }

    public Set<String> getAuthorityCodes() {
        return Set.copyOf(authorityCodes);
    }
}
