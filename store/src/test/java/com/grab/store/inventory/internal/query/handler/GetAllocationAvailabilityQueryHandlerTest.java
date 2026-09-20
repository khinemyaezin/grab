package com.grab.store.inventory.internal.query.handler;

import com.grab.store.inventory.internal.query.GetAllocationAvailabilityQuery;
import com.grab.store.inventory.internal.query.GetAllocationAvailabilityResult;
import com.inventory.domain.service.InventoryAllocationService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllocationAvailabilityQueryHandlerTest {

    @Mock
    private InventoryAllocationService inventoryAllocationService;

    @Mock
    private ProductVariantViewJpaRepository productVariantViewJpaRepository;

    @Mock
    private com.grab.framework.id.IdGenerator idGenerator;

    @InjectMocks
    private GetAllocationAvailabilityQueryHandler handler;

    @Test
    void handle_shouldTreatUntrackedSkuAsAllocatableWithoutInventory() {
        ProductView view = mock(ProductView.class);
        when(view.isManageInventory()).thenReturn(false);
        when(productVariantViewJpaRepository.findBySkuAndStatus("SKU-DIGITAL", ProductVariantViewEntity.STATUS_ACTIVE))
                .thenReturn(Optional.of(view));

        GetAllocationAvailabilityResult result = handler.handle(
                new GetAllocationAvailabilityQuery("SKU-DIGITAL", 3));

        assertThat(result.canAllocate()).isTrue();
        assertThat(result.availableQuantity()).isEqualTo(3);
        verify(inventoryAllocationService, never()).getAvailableForAllocation("SKU-DIGITAL", null);
    }

    @Test
    void handle_shouldUseLedgerWhenVariantManagesInventory() {
        ProductView view = mock(ProductView.class);
        when(view.isManageInventory()).thenReturn(true);
        when(productVariantViewJpaRepository.findBySkuAndStatus("SKU-1", ProductVariantViewEntity.STATUS_ACTIVE))
                .thenReturn(Optional.of(view));
        when(inventoryAllocationService.getAvailableForAllocation("SKU-1", null)).thenReturn(4);

        GetAllocationAvailabilityResult result = handler.handle(new GetAllocationAvailabilityQuery("SKU-1", 2));

        assertThat(result.canAllocate()).isTrue();
        assertThat(result.availableQuantity()).isEqualTo(4);
    }
}
