package com.grab.store.shared.security;

import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class SecurityManifestPublicationStateProvider {

    private SecurityManifestPublicationStateProvider() {
    }

    public static <E extends SecurityManifestPublicationState> Function<String, E> lockingProvider(
            JpaRepository<E, String> repository,
            Function<String, Optional<E>> lockByModuleKey,
            Function<String, E> entityFactory,
            PlatformTransactionManager transactionManager) {
        Objects.requireNonNull(repository, "repository must not be null");
        Objects.requireNonNull(lockByModuleKey, "lockByModuleKey must not be null");
        Objects.requireNonNull(entityFactory, "entityFactory must not be null");
        Objects.requireNonNull(transactionManager, "transactionManager must not be null");

        TransactionTemplate isolatedTransaction = new TransactionTemplate(transactionManager);
        isolatedTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return moduleKey -> {
            Optional<E> locked = lockByModuleKey.apply(moduleKey);
            if (locked.isPresent()) {
                return locked.get();
            }

            ensureInitialRowExists(repository, entityFactory, isolatedTransaction, moduleKey);

            return lockByModuleKey.apply(moduleKey)
                    .orElseThrow(() -> new IllegalStateException(
                            "Publication state row could not be locked after creation for module: " + moduleKey));
        };
    }

    private static <E extends SecurityManifestPublicationState> void ensureInitialRowExists(
            JpaRepository<E, String> repository,
            Function<String, E> entityFactory,
            TransactionTemplate isolatedTransaction,
            String moduleKey) {
        try {
            isolatedTransaction.executeWithoutResult(status -> {
                Optional<E> existing = repository.findById(moduleKey);
                if (existing.isEmpty()) {
                    E fresh = entityFactory.apply(moduleKey);
                    repository.saveAndFlush(fresh);
                }
            });
        } catch (DataIntegrityViolationException ignored) {
        }
    }
}
