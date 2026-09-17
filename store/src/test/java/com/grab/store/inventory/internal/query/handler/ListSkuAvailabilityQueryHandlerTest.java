package com.grab.store.inventory.internal.query.handler;

import com.grab.store.inventory.queries.ListSkuAvailabilityQuery;
import com.grab.store.inventory.queries.SkuAvailabilityResult;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.infrastructure.view.ProductView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSkuAvailabilityQueryHandlerTest {

    @Mock
    private InventoryAllocationService inventoryAllocationService;
    @Mock
    private ProductVariantViewJpaRepository productVariantViewJpaRepository;

    @InjectMocks
    private ListSkuAvailabilityQueryHandler handler;

    @Test
    void handle_treatsUntrackedSkusAsInStockAndBatchesTrackedSkus() {
        ProductView untracked = mock(ProductView.class);
        when(untracked.getSku()).thenReturn("SKU-DIGITAL");
        when(untracked.getStatus()).thenReturn(ProductVariantViewEntity.STATUS_ACTIVE);
        when(untracked.isManageInventory()).thenReturn(false);
        ProductView tracked = mock(ProductView.class);
        when(tracked.getSku()).thenReturn("SKU-1");
        when(tracked.getStatus()).thenReturn(ProductVariantViewEntity.STATUS_ACTIVE);
        when(tracked.isManageInventory()).thenReturn(true);
        when(productVariantViewJpaRepository.findAllBySkuIn(List.of("SKU-DIGITAL", "SKU-1")))
                .thenReturn(List.of(untracked, tracked));
        when(inventoryAllocationService.getAvailableForAllocation("SKU-1")).thenReturn(4);

        List<SkuAvailabilityResult> results = handler.handle(
                new ListSkuAvailabilityQuery(List.of("SKU-DIGITAL", "SKU-1")));

        assertThat(results).containsExactly(
                new SkuAvailabilityResult("SKU-DIGITAL", 1, true),
                new SkuAvailabilityResult("SKU-1", 4, true)
        );
        verify(inventoryAllocationService, never()).getAvailableForAllocation("SKU-DIGITAL");
    }
}
