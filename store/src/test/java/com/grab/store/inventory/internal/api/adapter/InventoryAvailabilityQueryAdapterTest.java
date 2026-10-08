package com.grab.store.inventory.internal.api.adapter;

import com.grab.store.inventory.internal.api.adapter.mapper.InventoryAvailabilityQueryMapper;
import com.grab.store.inventory.port.InventoryAvailabilityQuery.Availability;
import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.GetAllocationAvailabilityResult;
import com.inventory.application.model.read.ListSkusAtLocationQuery;
import com.inventory.application.port.inbound.GetAllocationAvailabilityUseCase;
import com.inventory.application.port.inbound.ListSkusAtLocationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryAvailabilityQueryAdapterTest {

    @Mock
    private GetAllocationAvailabilityUseCase getAllocationAvailabilityUseCase;
    @Mock
    private ListSkusAtLocationUseCase listSkusAtLocationUseCase;

    private InventoryAvailabilityQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryAvailabilityQueryAdapter(
                getAllocationAvailabilityUseCase,
                listSkusAtLocationUseCase,
                new InventoryAvailabilityQueryMapper()
        );
    }

    @Test
    void available_untrackedProduct_returnsUntrackedAvailability() {
        GetAllocationAvailabilityResult result = new GetAllocationAvailabilityResult(
                "sku-1", 0, false, 1, true
        );
        when(getAllocationAvailabilityUseCase.execute(any(GetAllocationAvailabilityQuery.class)))
                .thenReturn(result);

        Availability availability = adapter.available("sku-1", "sc-1");

        assertThat(availability.untracked()).isTrue();
        assertThat(availability.availableQty()).isZero();
    }

    @Test
    void available_trackedProduct_returnsAvailableQuantity() {
        GetAllocationAvailabilityResult result = new GetAllocationAvailabilityResult(
                "sku-1", 10, true, 1, false
        );
        when(getAllocationAvailabilityUseCase.execute(any(GetAllocationAvailabilityQuery.class)))
                .thenReturn(result);

        Availability availability = adapter.available("sku-1", "sc-1");

        assertThat(availability.untracked()).isFalse();
        assertThat(availability.availableQty()).isEqualTo(10);
    }

    @Test
    void skusAtLocation_validLocationId_returnsSkuList() {
        when(listSkusAtLocationUseCase.execute(any(ListSkusAtLocationQuery.class)))
                .thenReturn(List.of("sku-1", "sku-2"));

        List<String> skus = adapter.skusAtLocation("loc-1");

        assertThat(skus).containsExactly("sku-1", "sku-2");
    }
}
