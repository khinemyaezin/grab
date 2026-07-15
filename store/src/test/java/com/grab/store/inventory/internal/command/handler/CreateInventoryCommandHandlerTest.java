package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateInventoryCommandHandlerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ProductVariantViewJpaRepository productVariantViewRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private CreateInventoryCommandHandler handler;

    private CreateInventoryCommand command(String productVariantId) {
        return new CreateInventoryCommand(
                "SKU001",
                new CommonId("merchant-1"),
                productVariantId == null ? null : new CommonId(productVariantId),
                new CommonId("location-1"),
                0,
                null,
                null,
                null,
                null,
                new CommonId("user-1"),
                "merchant",
                "merchant-1"
        );
    }

    private void stubActiveLocation() {
        Location location = mock(Location.class);
        when(location.isActive()).thenReturn(true);
        when(locationRepository.findById(any())).thenReturn(Optional.of(location));
    }

    private ProductVariantViewEntity variantView(String status) {
        ProductVariantViewEntity view = new ProductVariantViewEntity();
        view.setVariantUuid("variant-1");
        view.setProductUuid("product-1");
        view.setSku("SKU001");
        view.setStatus(status);
        return view;
    }

    @Test
    void handle_shouldCreateInventory_whenProductVariantExistsInProjection() {
        stubActiveLocation();
        when(productVariantViewRepository.findByVariantUuid("variant-1"))
                .thenReturn(Optional.of(variantView(ProductVariantViewEntity.STATUS_ACTIVE)));
        when(inventoryRepository.existsBySkuAndLocation(anyString(), any())).thenReturn(false);
        when(idGenerator.generateId()).thenReturn(new CommonId("inventory-1"));

        InventoryItemResult result = handler.handle(command("variant-1"));

        assertThat(result.productVariantId()).isEqualTo("variant-1");
        verify(inventoryRepository).save(any());
    }

    @Test
    void handle_shouldReject_whenProductVariantNotInProjection() {
        stubActiveLocation();
        when(productVariantViewRepository.findByVariantUuid("variant-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command("variant-1")))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Product variant not found: variant-1");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void handle_shouldReject_whenProductVariantIsDeleted() {
        stubActiveLocation();
        when(productVariantViewRepository.findByVariantUuid("variant-1"))
                .thenReturn(Optional.of(variantView(ProductVariantViewEntity.STATUS_DELETED)));

        assertThatThrownBy(() -> handler.handle(command("variant-1")))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Product variant is deleted: variant-1");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void handle_shouldSkipVariantValidation_whenProductVariantIdIsNull() {
        stubActiveLocation();
        when(inventoryRepository.existsBySkuAndLocation(anyString(), any())).thenReturn(false);
        when(idGenerator.generateId()).thenReturn(new CommonId("inventory-1"));

        InventoryItemResult result = handler.handle(command(null));

        assertThat(result.productVariantId()).isNull();
        verify(productVariantViewRepository, never()).findByVariantUuid(anyString());
        verify(inventoryRepository).save(any());
    }
}
