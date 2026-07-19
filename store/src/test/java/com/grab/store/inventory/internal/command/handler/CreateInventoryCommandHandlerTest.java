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
import com.inventory.infrastructure.view.ProductView;
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
import static org.mockito.ArgumentMatchers.eq;
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
    private ProductVariantViewJpaRepository productVariantViewJpaRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private CreateInventoryCommandHandler handler;

    private CreateInventoryCommand command() {
        return new CreateInventoryCommand(
                "SKU001",
                new CommonId("merchant-1"),
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

    private ProductView variantView() {
        ProductView view = mock(ProductView.class);
        when(view.getVariantUuid()).thenReturn("variant-1");
        return view;
    }

    @Test
    void handle_shouldCreateInventory_whenActiveProductVariantExistsForSku() {
        stubActiveLocation();
        ProductView variant = variantView();
        when(productVariantViewJpaRepository.findBySkuAndStatus("SKU001", ProductVariantViewEntity.STATUS_ACTIVE))
                .thenReturn(Optional.of(variant));
        when(inventoryRepository.existsBySkuAndLocation(anyString(), any())).thenReturn(false);
        when(idGenerator.generateId()).thenReturn(new CommonId("inventory-1"));
        when(idGenerator.convertIdFrom("variant-1")).thenReturn(new CommonId("variant-1"));

        InventoryItemResult result = handler.handle(command());

        assertThat(result.productVariantId()).isEqualTo("variant-1");
        assertThat(result.sku()).isEqualTo("SKU001");
        verify(inventoryRepository).save(any());
    }

    @Test
    void handle_shouldReject_whenActiveProductVariantNotInProjection() {
        stubActiveLocation();
        when(productVariantViewJpaRepository.findBySkuAndStatus("SKU001", ProductVariantViewEntity.STATUS_ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command()))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Product variant not found for sku: SKU001");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void handle_shouldReject_whenOnlyDeletedProductVariantExistsForSku() {
        stubActiveLocation();
        when(productVariantViewJpaRepository.findBySkuAndStatus(eq("SKU001"), eq(ProductVariantViewEntity.STATUS_ACTIVE)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command()))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Product variant not found for sku: SKU001");

        verify(inventoryRepository, never()).save(any());
    }
}
