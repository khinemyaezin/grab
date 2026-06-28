package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Getter
public class Platform extends AggregateRoot<Id> {
    private final String code;
    private String name;
    private boolean active;
    private final Set<String> roleCodes;

    public Platform(Id id, String code, String name, boolean active, Set<String> roleCodes) {
        super(id);
        this.code = normalizeCode(code);
        this.name = validateName(name);
        this.active = active;
        this.roleCodes = normalizeRoleCodes(roleCodes);
    }

    public boolean supportsRole(String roleCode) {
        return active && roleCodes.contains(normalizeCode(roleCode));
    }

    public String requireSupportedRole(String roleCode) {
        String normalizedRoleCode = normalizeCode(roleCode);
        if (!active || !roleCodes.contains(normalizedRoleCode)) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.PlatformRoleNotSupported(code, normalizedRoleCode),
                    "Role is not available on the platform"
            );
        }
        return normalizedRoleCode;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public void addRole(String roleCode) {
        roleCodes.add(normalizeCode(roleCode));
    }

    public void removeRole(String roleCode) {
        roleCodes.remove(normalizeCode(roleCode));
    }

    public Set<String> getRoleCodes() {
        return Set.copyOf(roleCodes);
    }

    private static LinkedHashSet<String> normalizeRoleCodes(Set<String> roleCodes) {
        Objects.requireNonNull(roleCodes, "role codes are required");
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        roleCodes.forEach(roleCode -> normalized.add(normalizeCode(roleCode)));
        return normalized;
    }

    private static String normalizeCode(String value) {
        if (value == null) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidPlatformCode(String.valueOf((Object) null)),
                    "Invalid platform code"
            );
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z][A-Z0-9_]*")) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidPlatformCode(value),
                    "Invalid platform code"
            );
        }
        return normalized;
    }

    private static String validateName(String value) {
        if (value == null || value.isBlank()) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidPlatformName(),
                    "Platform name is required"
            );
        }
        return value.trim();
    }
}
