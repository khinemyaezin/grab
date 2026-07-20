package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.query.GetInventorySummaryQuery;
import com.grab.store.inventory.internal.query.GetInventorySummaryResult;
import com.inventory.infrastructure.repository.jpa.InventoryQueryRepository;
import com.inventory.infrastructure.view.CountBucketView;
import com.inventory.infrastructure.view.InventoryQuantityTotalsView;
import com.inventory.infrastructure.view.InventoryStatusBreakdownView;
import com.inventory.infrastructure.view.InventoryStockHealthBreakdownView;
import com.inventory.infrastructure.view.InventorySummaryScopeView;
import com.inventory.infrastructure.view.InventorySummaryView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInventorySummaryQueryHandlerTest {

    @Mock
    private InventoryQueryRepository inventoryQueryRepository;

    @InjectMocks
    private GetInventorySummaryQueryHandler handler;

    @Test
    void handle_withCounts_shouldComputePercentsRoundedToTwoDecimals() {
        InventorySummaryView view = new InventorySummaryView(
                new InventorySummaryScopeView("seller-1", "loc-1", "LOC-1", "Warehouse One"),
                120,
                new InventoryStatusBreakdownView(
                        new CountBucketView(90),
                        new CountBucketView(20),
                        new CountBucketView(5),
                        new CountBucketView(5)
                ),
                new InventoryStockHealthBreakdownView(
                        110,
                        new CountBucketView(70),
                        new CountBucketView(15),
                        new CountBucketView(20),
                        new CountBucketView(5)
                ),
                new InventoryQuantityTotalsView(5000, 200, 100, 50, 4750)
        );
        when(inventoryQueryRepository.summarize("seller-1", "loc-1")).thenReturn(view);

        GetInventorySummaryResult result = handler.handle(
                new GetInventorySummaryQuery(new CommonId("seller-1"), new CommonId("loc-1"))
        );

        assertThat(result.merchantId()).isEqualTo("seller-1");
        assertThat(result.locationCode()).isEqualTo("LOC-1");
        assertThat(result.locationName()).isEqualTo("Warehouse One");
        assertThat(result.totalItems()).isEqualTo(120);
        assertThat(result.status().active().percent()).isEqualTo(75.0);
        assertThat(result.status().outOfStock().percent()).isEqualTo(16.67);
        assertThat(result.health().eligibleItems()).isEqualTo(110);
        assertThat(result.health().inStock().percent()).isEqualTo(63.64);
        assertThat(result.health().critical().percent()).isEqualTo(4.55);
        assertThat(result.quantities().available()).isEqualTo(4750);
    }

    @Test
    void handle_withEmptyScope_shouldReturnZeroPercents() {
        InventorySummaryView view = new InventorySummaryView(
                new InventorySummaryScopeView("seller-1", null, null, null),
                0,
                new InventoryStatusBreakdownView(
                        new CountBucketView(0),
                        new CountBucketView(0),
                        new CountBucketView(0),
                        new CountBucketView(0)
                ),
                new InventoryStockHealthBreakdownView(
                        0,
                        new CountBucketView(0),
                        new CountBucketView(0),
                        new CountBucketView(0),
                        new CountBucketView(0)
                ),
                new InventoryQuantityTotalsView(0, 0, 0, 0, 0)
        );
        when(inventoryQueryRepository.summarize("seller-1", null)).thenReturn(view);

        GetInventorySummaryResult result = handler.handle(
                new GetInventorySummaryQuery(new CommonId("seller-1"), null)
        );

        assertThat(result.locationId()).isNull();
        assertThat(result.status().active().percent()).isEqualTo(0.0);
        assertThat(result.health().inStock().percent()).isEqualTo(0.0);
    }
}
