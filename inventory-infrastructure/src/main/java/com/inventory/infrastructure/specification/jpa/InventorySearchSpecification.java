package com.inventory.infrastructure.specification.jpa;

import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.meta.InventoryItemEntity_;
import com.inventory.infrastructure.view.InventoryItemView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InventorySearchSpecification {

    private final EntityManager entityManager;

    public InventorySearchSpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<InventoryItemView> search(InventorySearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        long total = countMatches(cb, criteria);
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        CriteriaQuery<InventoryItemView> dataQuery = cb.createQuery(InventoryItemView.class);
        Root<InventoryItemEntity> root = dataQuery.from(InventoryItemEntity.class);
        dataQuery.select(cb.construct(
                InventoryItemView.class,
                root.get(InventoryItemEntity_.UUID),
                root.get(InventoryItemEntity_.SKU),
                root.get(InventoryItemEntity_.MERCHANT_ID),
                root.get(InventoryItemEntity_.PRODUCT_VARIANT_ID),
                root.get(InventoryItemEntity_.LOCATION_ID),
                root.get(InventoryItemEntity_.ON_HAND),
                root.get(InventoryItemEntity_.RESERVED),
                root.get(InventoryItemEntity_.IN_TRANSIT),
                root.get(InventoryItemEntity_.DAMAGED),
                root.get(InventoryItemEntity_.SAFETY_STOCK),
                root.get(InventoryItemEntity_.REORDER_POINT),
                root.get(InventoryItemEntity_.REORDER_QUANTITY),
                root.get(InventoryItemEntity_.MAX_STOCK),
                root.get(InventoryItemEntity_.STATUS),
                root.get(InventoryItemEntity_.LAST_UPDATED)
        ));
        dataQuery.where(toPredicates(cb, root, criteria).toArray(new Predicate[0]));
        applySort(cb, dataQuery, root, pageable.getSort());

        TypedQuery<InventoryItemView> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<InventoryItemView> content = typedQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    private long countMatches(CriteriaBuilder cb, InventorySearchCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<InventoryItemEntity> countRoot = countQuery.from(InventoryItemEntity.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(toPredicates(cb, countRoot, criteria).toArray(new Predicate[0]));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> toPredicates(CriteriaBuilder cb, Root<InventoryItemEntity> root, InventorySearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(criteria.merchantId())) {
            predicates.add(cb.equal(root.get(InventoryItemEntity_.MERCHANT_ID), criteria.merchantId()));
        }
        if (StringUtils.hasText(criteria.sku())) {
            String pattern = "%" + criteria.sku().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(InventoryItemEntity_.SKU)), pattern));
        }
        if (StringUtils.hasText(criteria.locationId())) {
            predicates.add(cb.equal(root.get(InventoryItemEntity_.LOCATION_ID), criteria.locationId()));
        }
        if (criteria.status() != null) {
            predicates.add(cb.equal(root.get(InventoryItemEntity_.STATUS), criteria.status()));
        }

        return predicates;
    }

    private void applySort(CriteriaBuilder cb, CriteriaQuery<?> query, Root<InventoryItemEntity> root, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return;
        }
        List<Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Path<Object> path = root.get(order.getProperty());
            orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
        }
        query.orderBy(orders);
    }
}
