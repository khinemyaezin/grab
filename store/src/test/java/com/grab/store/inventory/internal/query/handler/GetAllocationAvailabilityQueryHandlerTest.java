package com.grab.store.inventory.internal.query.handler;

import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.GetAllocationAvailabilityResult;
import com.inventory.application.model.read.ProductView;
import com.inventory.application.service.GetAllocationAvailabilityService;
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
class GetAllocationAvailabilityServiceTest {

    @Mock
    private InventoryQueryPort inventoryQueryPort;

    @Mock
    private ProductVariantViewQueryPort productVariantViewQueryPort;

    @InjectMocks
    private GetAllocationAvailabilityService handler;

    @Test
    void handle_shouldTreatUntrackedSkuAsAllocatableWithoutInventory() {
        ProductView view = mock(ProductView.class);
        when(view.isManageInventory()).thenReturn(false);
        when(productVariantViewQueryPort.findActiveBySku("SKU-DIGITAL"))
                .thenReturn(Optional.of(view));

        GetAllocationAvailabilityResult result = handler.execute(
                new GetAllocationAvailabilityQuery("SKU-DIGITAL", 3));

        assertThat(result.canAllocate()).isTrue();
        assertThat(result.availableQuantity()).isEqualTo(3);
        verify(inventoryQueryPort, never()).sumAvailableForAllocation("SKU-DIGITAL", null);
    }

    @Test
    void handle_shouldUseLedgerWhenVariantManagesInventory() {
        ProductView view = mock(ProductView.class);
        when(view.isManageInventory()).thenReturn(true);
        when(productVariantViewQueryPort.findActiveBySku("SKU-1"))
                .thenReturn(Optional.of(view));
        when(inventoryQueryPort.sumAvailableForAllocation("SKU-1", null)).thenReturn(4);

        GetAllocationAvailabilityResult result = handler.execute(new GetAllocationAvailabilityQuery("SKU-1", 2));

        assertThat(result.canAllocate()).isTrue();
        assertThat(result.availableQuantity()).isEqualTo(4);
    }
}
