package com.grab.store.inventory.internal.event;

import com.grab.store.catalog.events.ProductDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductNameChangedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantAddedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantRestoredIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantUpdatedIntegrationEvent;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantViewProjectionEventListenerTest {

    @Mock
    private ProductVariantViewJpaRepository repository;

    @InjectMocks
    private ProductVariantViewProjectionEventListener listener;

    @Test
    void onVariantAdded_shouldInsertNewViewRow() {
        when(repository.findByVariantUuid("variant-1")).thenReturn(Optional.empty());

        listener.onVariantAdded(new ProductVariantAddedIntegrationEvent(
                "product-1", "variant-1", "SKU001", "T-Shirt", Instant.now(), 1));

        ArgumentCaptor<ProductVariantViewEntity> captor = ArgumentCaptor.forClass(ProductVariantViewEntity.class);
        verify(repository).save(captor.capture());
        ProductVariantViewEntity saved = captor.getValue();
        assertThat(saved.getVariantUuid()).isEqualTo("variant-1");
        assertThat(saved.getProductUuid()).isEqualTo("product-1");
        assertThat(saved.getSku()).isEqualTo("SKU001");
        assertThat(saved.getProductName()).isEqualTo("T-Shirt");
        assertThat(saved.getStatus()).isEqualTo(ProductVariantViewEntity.STATUS_ACTIVE);
    }

    @Test
    void onVariantAdded_shouldUpsertExistingRowOnRedelivery() {
        ProductVariantViewEntity existing = new ProductVariantViewEntity();
        existing.setVariantUuid("variant-1");
        existing.setProductUuid("product-1");
        existing.setSku("OLD");
        existing.setStatus(ProductVariantViewEntity.STATUS_DELETED);
        when(repository.findByVariantUuid("variant-1")).thenReturn(Optional.of(existing));

        listener.onVariantAdded(new ProductVariantAddedIntegrationEvent(
                "product-1", "variant-1", "SKU001", "T-Shirt", Instant.now(), 1));

        verify(repository).save(existing);
        assertThat(existing.getSku()).isEqualTo("SKU001");
        assertThat(existing.getStatus()).isEqualTo(ProductVariantViewEntity.STATUS_ACTIVE);
    }

    @Test
    void onVariantUpdated_shouldUpdateSku() {
        ProductVariantViewEntity existing = new ProductVariantViewEntity();
        existing.setVariantUuid("variant-1");
        existing.setProductUuid("product-1");
        existing.setSku("SKU001");
        existing.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        when(repository.findByVariantUuid("variant-1")).thenReturn(Optional.of(existing));

        listener.onVariantUpdated(new ProductVariantUpdatedIntegrationEvent(
                "product-1", "variant-1", "SKU002", Instant.now(), 1));

        verify(repository).save(existing);
        assertThat(existing.getSku()).isEqualTo("SKU002");
    }

    @Test
    void onVariantDeleted_shouldMarkRowDeleted() {
        ProductVariantViewEntity existing = new ProductVariantViewEntity();
        existing.setVariantUuid("variant-1");
        existing.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        when(repository.findByVariantUuid("variant-1")).thenReturn(Optional.of(existing));

        listener.onVariantDeleted(new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", Instant.now(), 1));

        verify(repository).save(existing);
        assertThat(existing.getStatus()).isEqualTo(ProductVariantViewEntity.STATUS_DELETED);
    }

    @Test
    void onVariantDeleted_shouldIgnoreUnknownVariant() {
        when(repository.findByVariantUuid("variant-1")).thenReturn(Optional.empty());

        listener.onVariantDeleted(new ProductVariantDeletedIntegrationEvent(
                "product-1", "variant-1", Instant.now(), 1));

        verify(repository, never()).save(any());
    }

    @Test
    void onVariantRestored_shouldMarkRowActive() {
        ProductVariantViewEntity existing = new ProductVariantViewEntity();
        existing.setVariantUuid("variant-1");
        existing.setStatus(ProductVariantViewEntity.STATUS_DELETED);
        when(repository.findByVariantUuid("variant-1")).thenReturn(Optional.of(existing));

        listener.onVariantRestored(new ProductVariantRestoredIntegrationEvent(
                "product-1", "variant-1", Instant.now(), 1));

        verify(repository).save(existing);
        assertThat(existing.getStatus()).isEqualTo(ProductVariantViewEntity.STATUS_ACTIVE);
    }

    @Test
    void onProductNameChanged_shouldRenameAllVariantRows() {
        ProductVariantViewEntity first = new ProductVariantViewEntity();
        first.setProductName("Old");
        ProductVariantViewEntity second = new ProductVariantViewEntity();
        second.setProductName("Old");
        when(repository.findAllByProductUuid("product-1")).thenReturn(List.of(first, second));

        listener.onProductNameChanged(new ProductNameChangedIntegrationEvent(
                "product-1", "New Name", Instant.now(), 1));

        verify(repository).saveAll(List.of(first, second));
        assertThat(first.getProductName()).isEqualTo("New Name");
        assertThat(second.getProductName()).isEqualTo("New Name");
    }

    @Test
    void onProductDeleted_shouldMarkAllVariantRowsDeleted() {
        ProductVariantViewEntity first = new ProductVariantViewEntity();
        first.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        ProductVariantViewEntity second = new ProductVariantViewEntity();
        second.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        when(repository.findAllByProductUuid("product-1")).thenReturn(List.of(first, second));

        listener.onProductDeleted(new ProductDeletedIntegrationEvent(
                "product-1", List.of("variant-1", "variant-2"), Instant.now(), 1));

        verify(repository).saveAll(List.of(first, second));
        assertThat(first.getStatus()).isEqualTo(ProductVariantViewEntity.STATUS_DELETED);
        assertThat(second.getStatus()).isEqualTo(ProductVariantViewEntity.STATUS_DELETED);
    }
}
