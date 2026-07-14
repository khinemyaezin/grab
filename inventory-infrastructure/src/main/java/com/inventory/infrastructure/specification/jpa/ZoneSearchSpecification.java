package com.inventory.infrastructure.specification.jpa;

import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.entity.meta.LocationEntity_;
import com.inventory.infrastructure.entity.meta.ZoneEntity_;
import com.inventory.infrastructure.view.ZoneView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ZoneSearchSpecification {

    private final EntityManager entityManager;

    public ZoneSearchSpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<ZoneView> search(ZoneSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        long total = countMatches(cb, criteria);
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        CriteriaQuery<ZoneView> dataQuery = cb.createQuery(ZoneView.class);
        Root<ZoneEntity> root = dataQuery.from(ZoneEntity.class);
        dataQuery.select(cb.construct(
                ZoneView.class,
                root.get(ZoneEntity_.UUID),
                root.get(ZoneEntity_.CODE),
                root.get(ZoneEntity_.NAME),
                root.get(ZoneEntity_.TYPE),
                root.get(ZoneEntity_.ACTIVE),
                root.get(ZoneEntity_.LOCATION_ID)
        ));
        dataQuery.where(toPredicates(cb, dataQuery, root, criteria).toArray(new Predicate[0]));
        applySort(cb, dataQuery, root, pageable.getSort());

        TypedQuery<ZoneView> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<ZoneView> content = typedQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    private long countMatches(CriteriaBuilder cb, ZoneSearchCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ZoneEntity> countRoot = countQuery.from(ZoneEntity.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(toPredicates(cb, countQuery, countRoot, criteria).toArray(new Predicate[0]));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> toPredicates(CriteriaBuilder cb, CriteriaQuery<?> query, Root<ZoneEntity> root, ZoneSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(criteria.merchantId())) {
            predicates.add(merchantScopePredicate(cb, query, root, criteria.merchantId()));
        }
        if (StringUtils.hasText(criteria.locationId())) {
            predicates.add(cb.equal(root.get(ZoneEntity_.LOCATION_ID), criteria.locationId()));
        }
        if (StringUtils.hasText(criteria.query())) {
            String pattern = "%" + criteria.query().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get(ZoneEntity_.CODE)), pattern),
                    cb.like(cb.lower(root.get(ZoneEntity_.NAME)), pattern)
            ));
        }
        if (criteria.type() != null) {
            predicates.add(cb.equal(root.get(ZoneEntity_.TYPE), criteria.type()));
        }
        if (criteria.active() != null) {
            predicates.add(cb.equal(root.get(ZoneEntity_.ACTIVE), criteria.active()));
        }

        return predicates;
    }

    private Predicate merchantScopePredicate(CriteriaBuilder cb, CriteriaQuery<?> query, Root<ZoneEntity> root, String merchantId) {
        Subquery<Long> locationSubquery = query.subquery(Long.class);
        Root<LocationEntity> location = locationSubquery.from(LocationEntity.class);
        locationSubquery.select(cb.literal(1L));
        locationSubquery.where(
                cb.equal(location.get(LocationEntity_.UUID), root.get(ZoneEntity_.LOCATION_ID)),
                cb.equal(location.get(LocationEntity_.MERCHANT_ID), merchantId)
        );
        return cb.exists(locationSubquery);
    }

    private void applySort(CriteriaBuilder cb, CriteriaQuery<?> query, Root<ZoneEntity> root, Sort sort) {
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
