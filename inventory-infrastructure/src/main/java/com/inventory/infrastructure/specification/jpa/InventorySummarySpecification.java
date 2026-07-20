package com.inventory.infrastructure.specification.jpa;

import com.inventory.domain.enums.InventoryStatus;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.meta.InventoryItemEntity_;
import com.inventory.infrastructure.view.InventorySummaryAggregationView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InventorySummarySpecification {

    private static final String ALIAS_TOTAL_ITEMS = "totalItems";
    private static final String ALIAS_ACTIVE_COUNT = "activeCount";
    private static final String ALIAS_STATUS_OUT_OF_STOCK_COUNT = "statusOutOfStockCount";
    private static final String ALIAS_SUSPENDED_COUNT = "suspendedCount";
    private static final String ALIAS_DISCONTINUED_COUNT = "discontinuedCount";
    private static final String ALIAS_HEALTH_ELIGIBLE_ITEMS = "healthEligibleItems";
    private static final String ALIAS_HEALTH_IN_STOCK = "healthInStock";
    private static final String ALIAS_HEALTH_LOW_STOCK = "healthLowStock";
    private static final String ALIAS_HEALTH_OUT_OF_STOCK = "healthOutOfStock";
    private static final String ALIAS_HEALTH_CRITICAL = "healthCritical";
    private static final String ALIAS_ON_HAND = "onHand";
    private static final String ALIAS_RESERVED = "reserved";
    private static final String ALIAS_IN_TRANSIT = "inTransit";
    private static final String ALIAS_DAMAGED = "damaged";
    private static final String ALIAS_AVAILABLE = "available";

    private final EntityManager entityManager;

    public InventorySummarySpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public InventorySummaryAggregationView aggregate(String merchantId, String locationId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<InventoryItemEntity> root = query.from(InventoryItemEntity.class);

        Expression<Integer> onHand = root.get(InventoryItemEntity_.ON_HAND);
        Expression<Integer> reserved = root.get(InventoryItemEntity_.RESERVED);
        Expression<Integer> inTransit = root.get(InventoryItemEntity_.IN_TRANSIT);
        Expression<Integer> damaged = root.get(InventoryItemEntity_.DAMAGED);
        Expression<Integer> safetyStock = root.get(InventoryItemEntity_.SAFETY_STOCK);
        Expression<Integer> reorderPoint = root.get(InventoryItemEntity_.REORDER_POINT);
        Expression<InventoryStatus> status = root.get(InventoryItemEntity_.STATUS);

        Expression<Integer> availableRaw = cb.diff(cb.diff(onHand, reserved), damaged);
        Expression<Integer> available = cb.<Integer>selectCase()
                .when(cb.lessThan(availableRaw, 0), 0)
                .otherwise(availableRaw);

        Predicate sellable = status.in(InventoryStatus.ACTIVE, InventoryStatus.OUT_OF_STOCK);

        Predicate outOfStockHealth = cb.and(sellable, cb.lessThanOrEqualTo(available, 0));
        Predicate criticalHealth = cb.and(
                sellable,
                cb.greaterThan(available, 0),
                cb.lessThanOrEqualTo(available, safetyStock)
        );
        Predicate lowStockHealth = cb.and(
                sellable,
                cb.greaterThan(available, safetyStock),
                cb.lessThanOrEqualTo(available, reorderPoint)
        );
        Predicate inStockHealth = cb.and(sellable, cb.greaterThan(available, reorderPoint));

        query.multiselect(
                cb.count(root).alias(ALIAS_TOTAL_ITEMS),
                sumCase(cb, cb.equal(status, InventoryStatus.ACTIVE)).alias(ALIAS_ACTIVE_COUNT),
                sumCase(cb, cb.equal(status, InventoryStatus.OUT_OF_STOCK)).alias(ALIAS_STATUS_OUT_OF_STOCK_COUNT),
                sumCase(cb, cb.equal(status, InventoryStatus.SUSPENDED)).alias(ALIAS_SUSPENDED_COUNT),
                sumCase(cb, cb.equal(status, InventoryStatus.DISCONTINUED)).alias(ALIAS_DISCONTINUED_COUNT),
                sumCase(cb, sellable).alias(ALIAS_HEALTH_ELIGIBLE_ITEMS),
                sumCase(cb, inStockHealth).alias(ALIAS_HEALTH_IN_STOCK),
                sumCase(cb, lowStockHealth).alias(ALIAS_HEALTH_LOW_STOCK),
                sumCase(cb, outOfStockHealth).alias(ALIAS_HEALTH_OUT_OF_STOCK),
                sumCase(cb, criticalHealth).alias(ALIAS_HEALTH_CRITICAL),
                coalesceSum(cb, onHand).alias(ALIAS_ON_HAND),
                coalesceSum(cb, reserved).alias(ALIAS_RESERVED),
                coalesceSum(cb, inTransit).alias(ALIAS_IN_TRANSIT),
                coalesceSum(cb, damaged).alias(ALIAS_DAMAGED),
                coalesceSum(cb, available).alias(ALIAS_AVAILABLE)
        );
        query.where(toPredicates(cb, root, merchantId, locationId).toArray(new Predicate[0]));

        Tuple tuple = entityManager.createQuery(query).getSingleResult();
        return toAggregationView(tuple);
    }

    private List<Predicate> toPredicates(
            CriteriaBuilder cb,
            Root<InventoryItemEntity> root,
            String merchantId,
            String locationId
    ) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(merchantId)) {
            predicates.add(cb.equal(root.get(InventoryItemEntity_.MERCHANT_ID), merchantId));
        }
        if (StringUtils.hasText(locationId)) {
            predicates.add(cb.equal(root.get(InventoryItemEntity_.LOCATION_ID), locationId));
        }
        return predicates;
    }

    private Expression<Long> sumCase(CriteriaBuilder cb, Predicate when) {
        Expression<Long> caseExpr = cb.<Long>selectCase()
                .when(when, 1L)
                .otherwise(0L);
        return cb.coalesce(cb.sum(caseExpr), 0L);
    }

    private Expression<Number> coalesceSum(CriteriaBuilder cb, Expression<Integer> expression) {
        return cb.coalesce(cb.sum(expression), 0);
    }

    private static InventorySummaryAggregationView toAggregationView(Tuple tuple) {
        return new InventorySummaryAggregationView(
                toLong(tuple.get(ALIAS_TOTAL_ITEMS)),
                toLong(tuple.get(ALIAS_ACTIVE_COUNT)),
                toLong(tuple.get(ALIAS_STATUS_OUT_OF_STOCK_COUNT)),
                toLong(tuple.get(ALIAS_SUSPENDED_COUNT)),
                toLong(tuple.get(ALIAS_DISCONTINUED_COUNT)),
                toLong(tuple.get(ALIAS_HEALTH_ELIGIBLE_ITEMS)),
                toLong(tuple.get(ALIAS_HEALTH_IN_STOCK)),
                toLong(tuple.get(ALIAS_HEALTH_LOW_STOCK)),
                toLong(tuple.get(ALIAS_HEALTH_OUT_OF_STOCK)),
                toLong(tuple.get(ALIAS_HEALTH_CRITICAL)),
                toLong(tuple.get(ALIAS_ON_HAND)),
                toLong(tuple.get(ALIAS_RESERVED)),
                toLong(tuple.get(ALIAS_IN_TRANSIT)),
                toLong(tuple.get(ALIAS_DAMAGED)),
                toLong(tuple.get(ALIAS_AVAILABLE))
        );
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return ((Number) value).longValue();
    }
}
