package com.grab.store.shared.security;

import com.inventory.adapter.persistence.entity.InventorySecurityManifestPublicationEntity;
import com.inventory.adapter.persistence.repository.jpa.InventorySecurityManifestPublicationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityManifestPublicationStateProviderTest {

    @Mock
    private InventorySecurityManifestPublicationJpaRepository repository;

    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        transactionManager = new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
            }

            @Override
            public void rollback(TransactionStatus status) {
            }
        };
    }

    @Test
    void lockingProvider_whenRowAlreadyExists_returnsLockedEntityWithoutIsolatedInsert() {
        InventorySecurityManifestPublicationEntity existing = new InventorySecurityManifestPublicationEntity("inventory");
        when(repository.lockByModuleKey("inventory")).thenReturn(Optional.of(existing));

        Function<String, InventorySecurityManifestPublicationEntity> provider =
                SecurityManifestPublicationStateProvider.lockingProvider(
                        repository,
                        repository::lockByModuleKey,
                        InventorySecurityManifestPublicationEntity::new,
                        transactionManager);

        InventorySecurityManifestPublicationEntity result = provider.apply("inventory");

        assertThat(result).isSameAs(existing);
        verify(repository, times(1)).lockByModuleKey("inventory");
        verify(repository, never()).findById(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void lockingProvider_whenRowMissing_insertsBaselineRowAndAcquiresLock() {
        InventorySecurityManifestPublicationEntity created = new InventorySecurityManifestPublicationEntity("inventory");
        AtomicInteger lockAttempts = new AtomicInteger();

        when(repository.lockByModuleKey("inventory")).thenAnswer(invocation -> {
            if (lockAttempts.incrementAndGet() == 1) {
                return Optional.empty();
            }
            return Optional.of(created);
        });
        when(repository.findById("inventory")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenReturn(created);

        Function<String, InventorySecurityManifestPublicationEntity> provider =
                SecurityManifestPublicationStateProvider.lockingProvider(
                        repository,
                        repository::lockByModuleKey,
                        InventorySecurityManifestPublicationEntity::new,
                        transactionManager);

        InventorySecurityManifestPublicationEntity result = provider.apply("inventory");

        assertThat(result).isSameAs(created);
        verify(repository, times(2)).lockByModuleKey("inventory");
        verify(repository, times(1)).findById("inventory");
        verify(repository, times(1)).saveAndFlush(any(InventorySecurityManifestPublicationEntity.class));
    }

    @Test
    void lockingProvider_whenConcurrentCollisionOccurs_swallowsDataIntegrityViolationAndAcquiresLock() {
        InventorySecurityManifestPublicationEntity fromConcurrentReplica = new InventorySecurityManifestPublicationEntity("inventory");
        AtomicInteger lockAttempts = new AtomicInteger();

        when(repository.lockByModuleKey("inventory")).thenAnswer(invocation -> {
            if (lockAttempts.incrementAndGet() == 1) {
                return Optional.empty();
            }
            return Optional.of(fromConcurrentReplica);
        });
        when(repository.findById("inventory")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        Function<String, InventorySecurityManifestPublicationEntity> provider =
                SecurityManifestPublicationStateProvider.lockingProvider(
                        repository,
                        repository::lockByModuleKey,
                        InventorySecurityManifestPublicationEntity::new,
                        transactionManager);

        InventorySecurityManifestPublicationEntity result = provider.apply("inventory");

        assertThat(result).isSameAs(fromConcurrentReplica);
        verify(repository, times(2)).lockByModuleKey("inventory");
        verify(repository, times(1)).findById("inventory");
        verify(repository, times(1)).saveAndFlush(any());
    }

    @Test
    void lockingProvider_whenLockStillFailsAfterCreation_throwsIllegalStateException() {
        when(repository.lockByModuleKey("inventory")).thenReturn(Optional.empty());
        when(repository.findById("inventory")).thenReturn(Optional.empty());

        Function<String, InventorySecurityManifestPublicationEntity> provider =
                SecurityManifestPublicationStateProvider.lockingProvider(
                        repository,
                        repository::lockByModuleKey,
                        InventorySecurityManifestPublicationEntity::new,
                        transactionManager);

        assertThatThrownBy(() -> provider.apply("inventory"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Publication state row could not be locked after creation for module: inventory");
    }
}
