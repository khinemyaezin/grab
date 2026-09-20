package com.inventory.application.service;

import com.inventory.application.port.inbound.GetInventorySummaryUseCase;

import com.inventory.application.model.read.GetInventorySummaryQuery;
import com.inventory.application.model.read.GetInventorySummaryResult;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.model.read.CountBucketView;
import com.inventory.application.model.read.InventorySummaryView;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RequiredArgsConstructor
public class GetInventorySummaryService implements GetInventorySummaryUseCase {

    private final InventoryQueryPort inventoryQueryPort;

            public GetInventorySummaryResult execute(GetInventorySummaryQuery query) {
        String merchantId = query.merchantId().getValue();
        String locationId = query.locationId() == null ? null : query.locationId().getValue();
        InventorySummaryView view = inventoryQueryPort.summarize(merchantId, locationId);
        return toResult(view);
    }

        public Class<GetInventorySummaryQuery> getQueryType() {
        return GetInventorySummaryQuery.class;
    }

    private GetInventorySummaryResult toResult(InventorySummaryView view) {
        long totalItems = view.totalItems();
        long eligibleItems = view.health().eligibleItems();

        GetInventorySummaryResult.StatusBreakdown status = new GetInventorySummaryResult.StatusBreakdown(
                toCountPercent(view.status().active(), totalItems),
                toCountPercent(view.status().outOfStock(), totalItems),
                toCountPercent(view.status().suspended(), totalItems),
                toCountPercent(view.status().discontinued(), totalItems)
        );
        GetInventorySummaryResult.HealthBreakdown health = new GetInventorySummaryResult.HealthBreakdown(
                eligibleItems,
                toCountPercent(view.health().inStock(), eligibleItems),
                toCountPercent(view.health().lowStock(), eligibleItems),
                toCountPercent(view.health().outOfStock(), eligibleItems),
                toCountPercent(view.health().critical(), eligibleItems)
        );
        GetInventorySummaryResult.QuantityTotals quantities = new GetInventorySummaryResult.QuantityTotals(
                view.quantities().onHand(),
                view.quantities().reserved(),
                view.quantities().inTransit(),
                view.quantities().damaged(),
                view.quantities().available()
        );
        return new GetInventorySummaryResult(
                view.scope().merchantId(),
                view.scope().locationId(),
                view.scope().locationCode(),
                view.scope().locationName(),
                totalItems,
                status,
                health,
                quantities
        );
    }

    private static GetInventorySummaryResult.CountPercent toCountPercent(CountBucketView bucket, long denominator) {
        long count = bucket.count();
        return new GetInventorySummaryResult.CountPercent(count, percentOf(count, denominator));
    }

    private static double percentOf(long count, long denominator) {
        if (denominator <= 0L) {
            return 0.0d;
        }
        return BigDecimal.valueOf(count)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
