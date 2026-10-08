package com.identity.adapter.persistence.adapter;

import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.adapter.persistence.entity.ScopeManifestEntity;
import com.identity.adapter.persistence.repository.jpa.ScopeManifestJpaRepository;
import com.identity.domain.port.outbound.ScopeManifestRepository;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ScopeManifestRepositoryAdapter implements ScopeManifestRepository {
    private final ScopeManifestJpaRepository repository;
    private final PersistenceExecutor executor;

    @Override
    public boolean apply(String moduleKey, int manifestVersion, List<ScopeDeclaration> scopes) {
        return executor.command("ScopeManifest", () -> {
            List<ScopeManifestEntity> current = repository.findByModuleKeyForUpdate(moduleKey);
            int currentVersion = current.stream()
                    .mapToInt(ScopeManifestEntity::getManifestVersion)
                    .max()
                    .orElse(0);
            if (manifestVersion < currentVersion) {
                return false;
            }
            if (manifestVersion == currentVersion && currentVersion > 0) {
                List<ScopeManifestEntity> activeCurrent = current.stream()
                        .filter(ScopeManifestEntity::isActive)
                        .toList();
                return sameSnapshot(activeCurrent, scopes);
            }

            Set<String> declaredKeys = scopes.stream()
                    .map(scope -> scope.scopeKey().trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            boolean hasActiveMissing = current.stream()
                    .anyMatch(scope -> scope.isActive() && !declaredKeys.contains(scope.getScopeKey()));
            if (hasActiveMissing) {
                return false;
            }

            for (ScopeDeclaration declaration : scopes) {
                String scopeKey = declaration.scopeKey().trim().toLowerCase(Locale.ROOT);
                if ("global".equals(scopeKey) || scopeKey.equals(declaration.parentScopeKey())) {
                    return false;
                }
                String parent = declaration.parentScopeKey();
                if (parent != null) {
                    String lowerParent = parent.toLowerCase(Locale.ROOT);
                    if (!declaredKeys.contains(lowerParent)
                            && !"global".equals(parent)
                            && !repository.existsByScopeKeyAndActiveTrue(lowerParent)) {
                        return false;
                    }
                }
                boolean ownedByOtherModule = repository.findByScopeKey(scopeKey).stream()
                        .anyMatch(existing -> !moduleKey.equals(existing.getModuleKey()));
                if (ownedByOtherModule) {
                    return false;
                }
                Optional<ScopeManifestEntity> existingOwned = current.stream()
                        .filter(existing -> scopeKey.equals(existing.getScopeKey()))
                        .findFirst();
                if (existingOwned.isPresent()) {
                    ScopeManifestEntity existing = existingOwned.get();
                    if (!Objects.equals(existing.getParentScopeKey(), declaration.parentScopeKey())) {
                        return false;
                    }
                    if (!existing.isActive() && declaration.lifecycle() == ScopeDeclaration.Lifecycle.ACTIVE) {
                        return false;
                    }
                }
            }

            for (ScopeDeclaration declaration : scopes) {
                String scopeKey = declaration.scopeKey().trim().toLowerCase(Locale.ROOT);
                ScopeManifestEntity entity = current.stream()
                        .filter(existing -> scopeKey.equals(existing.getScopeKey()))
                        .findFirst()
                        .orElseGet(ScopeManifestEntity::new);
                entity.setModuleKey(moduleKey);
                entity.setScopeKey(scopeKey);
                entity.setParentScopeKey(declaration.parentScopeKey());
                entity.setManifestVersion(manifestVersion);
                entity.setActive(declaration.lifecycle() == ScopeDeclaration.Lifecycle.ACTIVE);
                repository.save(entity);
            }
            return true;
        });
    }

    @Override
    public List<ScopeDeclaration> loadActive() {
        return executor.query("ScopeManifest", () -> {
            List<ScopeManifestEntity> active = repository.findByActiveTrue();
            return active.stream()
                    .map(scope -> new ScopeDeclaration(scope.getScopeKey(), scope.getParentScopeKey()))
                    .toList();
        });
    }

    @Override
    public Map<String, String> loadGraph() {
        return executor.query("ScopeManifest", () -> {
            List<ScopeManifestEntity> all = repository.findAll();
            Map<String, String> graph = new HashMap<>();
            all.forEach(scope -> graph.put(scope.getScopeKey(), scope.getParentScopeKey()));
            return Collections.unmodifiableMap(graph);
        });
    }

    @Override
    public Map<String, String> loadOwners() {
        return executor.query("ScopeManifest", () -> {
            List<ScopeManifestEntity> all = repository.findAll();
            return all.stream().collect(Collectors.toUnmodifiableMap(
                    ScopeManifestEntity::getScopeKey,
                    ScopeManifestEntity::getModuleKey,
                    (left, right) -> left));
        });
    }

    private boolean sameSnapshot(List<ScopeManifestEntity> current, List<ScopeDeclaration> scopes) {
        List<String> expected = scopes.stream()
                .map(scope -> scope.scopeKey().trim().toLowerCase(Locale.ROOT) + "\u0000" + scope.parentScopeKey())
                .sorted()
                .toList();
        List<String> actual = current.stream()
                .filter(ScopeManifestEntity::isActive)
                .map(scope -> scope.getScopeKey() + "\u0000" + scope.getParentScopeKey())
                .sorted(Comparator.naturalOrder())
                .toList();
        return expected.equals(actual);
    }
}
