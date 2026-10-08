package com.identity.domain.valueobject;

import com.grab.framework.security.ScopeDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class ScopeHierarchy {
    private static final AtomicReference<ScopeHierarchy> CURRENT =
            new AtomicReference<>(empty());

    private final Map<String, String> parentOf;
    private final Map<String, String> ownerOf;
    private final Set<String> inactive;

    public ScopeHierarchy(Map<String, String> parentOf, Map<String, String> ownerOf) {
        this(parentOf, ownerOf, Set.of());
    }

    public ScopeHierarchy(Map<String, String> parentOf, Map<String, String> ownerOf, Set<String> inactive) {
        this.inactive = Set.copyOf(inactive);
        this.parentOf = Collections.unmodifiableMap(new LinkedHashMap<>(parentOf));
        this.ownerOf = Collections.unmodifiableMap(new LinkedHashMap<>(ownerOf));
    }

    public static ScopeHierarchy current() {
        return CURRENT.get();
    }

    public static void setCurrent(ScopeHierarchy hierarchy) {
        Objects.requireNonNull(hierarchy, "hierarchy is required");
        CURRENT.set(hierarchy);
    }

    public static void reset() {
        CURRENT.set(empty());
    }

    public static ScopeHierarchy empty() {
        return new ScopeHierarchy(Map.of(), Map.of());
    }

    public static void register(ScopeDeclaration declaration) {
        Objects.requireNonNull(declaration, "scope declaration is required");
        CURRENT.updateAndGet(existing -> existing.with(declaration));
    }

    public static void registerAll(Collection<ScopeDeclaration> declarations) {
        Objects.requireNonNull(declarations, "scope declarations are required");
        CURRENT.updateAndGet(existing -> existing.withAll(declarations));
    }

    public static void replaceModule(String moduleKey, Collection<ScopeDeclaration> declarations) {
        Objects.requireNonNull(moduleKey, "module key is required");
        Objects.requireNonNull(declarations, "scope declarations are required");
        CURRENT.updateAndGet(existing -> existing.withReplacedModule(moduleKey, declarations));
    }

    public static boolean isAncestorOrSelf(String actorKey, String targetKey) {
        return CURRENT.get().checkAncestorOrSelf(actorKey, targetKey);
    }

    public static Optional<String> parentOf(String scopeKey) {
        return CURRENT.get().getParent(scopeKey);
    }

    public static Set<String> allKeys() {
        return CURRENT.get().getAllKeys();
    }

    public static boolean isRegistered(String scopeKey) {
        return CURRENT.get().isKeyRegistered(scopeKey);
    }

    public ScopeHierarchy with(ScopeDeclaration declaration) {
        return withAll(Set.of(declaration));
    }

    public ScopeHierarchy withAll(Collection<ScopeDeclaration> declarations) {
        var newParentOf = new LinkedHashMap<>(this.parentOf);
        var newOwnerOf = new LinkedHashMap<>(this.ownerOf);
        for (ScopeDeclaration declaration : declarations) {
            Objects.requireNonNull(declaration, "scope declaration is required");
            var scopeKey = normalize(declaration.scopeKey());
            var parentScopeKey = normalizeParent(declaration.parentScopeKey());
            validateScopeKey(scopeKey);
            if (scopeKey.equals(parentScopeKey)) {
                throw new IllegalArgumentException("scope cannot be its own parent: " + scopeKey);
            }
            if (newParentOf.containsKey(scopeKey) && !Objects.equals(newParentOf.get(scopeKey), parentScopeKey)) {
                throw new IllegalArgumentException("scope parent conflict: " + scopeKey);
            }
            newParentOf.put(scopeKey, parentScopeKey);
            if (hasCycle(scopeKey, newParentOf)) {
                throw new IllegalArgumentException("scope hierarchy contains a cycle: " + scopeKey);
            }
        }
        return new ScopeHierarchy(newParentOf, newOwnerOf);
    }

    public ScopeHierarchy withReplacedModule(String moduleKey, Collection<ScopeDeclaration> declarations) {
        var newParentOf = new LinkedHashMap<>(this.parentOf);
        var newOwnerOf = new LinkedHashMap<>(this.ownerOf);
        newOwnerOf.entrySet().removeIf(entry -> moduleKey.equals(entry.getValue()));
        for (ScopeDeclaration declaration : declarations) {
            var scopeKey = normalize(declaration.scopeKey());
            var parentScopeKey = normalizeParent(declaration.parentScopeKey());
            validateScopeKey(scopeKey);
            if (ScopeKey.GLOBAL_VALUE.equals(scopeKey) || scopeKey.equals(parentScopeKey)) {
                throw new IllegalArgumentException("invalid scope declaration: " + scopeKey);
            }
            var existingOwner = newOwnerOf.get(scopeKey);
            if (existingOwner != null && !moduleKey.equals(existingOwner)) {
                throw new IllegalArgumentException("scope key already owned: " + scopeKey);
            }
            newParentOf.put(scopeKey, parentScopeKey);
            newOwnerOf.put(scopeKey, moduleKey);
            if (hasCycle(scopeKey, newParentOf)) {
                throw new IllegalArgumentException("scope hierarchy contains a cycle: " + scopeKey);
            }
        }
        return new ScopeHierarchy(newParentOf, newOwnerOf);
    }

    public boolean checkAncestorOrSelf(String actorKey, String targetKey) {
        if (!isEffective(actorKey) || !isEffective(targetKey)) {
            return false;
        }
        if (actorKey.equals(targetKey)) {
            return true;
        }
        if (ScopeKey.GLOBAL_VALUE.equals(actorKey)) {
            return true;
        }
        String current = targetKey;
        var visited = new LinkedHashSet<String>();
        while (current != null) {
            if (!visited.add(current)) {
                return false;
            }
            current = parentOf.get(current);
            if (actorKey.equals(current)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEffective(String key) {
        if (key == null || key.isBlank()) return false;
        var visited = new LinkedHashSet<String>();
        String current = key;
        while (current != null && !ScopeKey.GLOBAL_VALUE.equals(current)) {
            if (!visited.add(current) || !parentOf.containsKey(current) || inactive.contains(current)) {
                return false;
            }
            current = parentOf.get(current);
        }
        return true;
    }

    public Optional<String> getParent(String scopeKey) {
        return Optional.ofNullable(parentOf.get(scopeKey));
    }

    public Set<String> getAllKeys() {
        var keys = new LinkedHashSet<String>();
        keys.add(ScopeKey.GLOBAL_VALUE);
        keys.addAll(parentOf.keySet());
        return Set.copyOf(keys);
    }

    public boolean isKeyRegistered(String scopeKey) {
        return ScopeKey.GLOBAL_VALUE.equals(scopeKey) || parentOf.containsKey(scopeKey);
    }

    private static String normalize(String scopeKey) {
        if (scopeKey == null) {
            throw new IllegalArgumentException("scope key is required");
        }
        return scopeKey.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeParent(String parentScopeKey) {
        if (parentScopeKey == null || parentScopeKey.isBlank()) {
            return null;
        }
        return normalize(parentScopeKey);
    }

    private static void validateScopeKey(String scopeKey) {
        if (!scopeKey.matches("[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)+")) {
            throw new IllegalArgumentException("invalid scope key: " + scopeKey);
        }
    }

    private static boolean hasCycle(String startKey, Map<String, String> parents) {
        var visited = new LinkedHashSet<String>();
        String current = startKey;
        while (current != null) {
            if (!visited.add(current)) {
                return true;
            }
            current = parents.get(current);
        }
        return false;
    }
}
