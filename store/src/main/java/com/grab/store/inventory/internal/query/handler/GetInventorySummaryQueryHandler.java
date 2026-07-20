package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetInventorySummaryQuery;
import com.grab.store.inventory.internal.query.GetInventorySummaryResult;
import com.inventory.infrastructure.repository.jpa.InventoryQueryRepository;
import com.inventory.infrastructure.view.CountBucketView;
import com.inventory.infrastructure.view.InventorySummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class GetInventorySummaryQueryHandler
        implements QueryHandler<GetInventorySummaryQuery, GetInventorySummaryResult> {

    private final InventoryQueryRepository inventoryQueryRepository;

    @Override
    @InventoryReadTransactional
    public GetInventorySummaryResult handle(GetInventorySummaryQuery query) {
        String merchantId = query.merchantId().getValue();
        String locationId = query.locationId() == null ? null : query.locationId().getValue();
        InventorySummaryView view = inventoryQueryRepository.summarize(merchantId, locationId);
        return toResult(view);
    }

    @Override
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
