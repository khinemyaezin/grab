package com.inventory.infrastructure.specification.jpa;

import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.entity.meta.BinEntity_;
import com.inventory.infrastructure.entity.meta.LocationEntity_;
import com.inventory.infrastructure.entity.meta.ZoneEntity_;
import com.inventory.infrastructure.view.BinView;
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

public class BinSearchSpecification {

    private final EntityManager entityManager;

    public BinSearchSpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<BinView> search(BinSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        long total = countMatches(cb, criteria);
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        CriteriaQuery<BinView> dataQuery = cb.createQuery(BinView.class);
        Root<BinEntity> root = dataQuery.from(BinEntity.class);
        dataQuery.select(cb.construct(
                BinView.class,
                root.get(BinEntity_.UUID),
                root.get(BinEntity_.CODE),
                root.get(BinEntity_.NAME),
                root.get(BinEntity_.MAX_CAPACITY),
                root.get(BinEntity_.ACTIVE),
                root.get(BinEntity_.ZONE_ID)
        ));
        dataQuery.where(toPredicates(cb, dataQuery, root, criteria).toArray(new Predicate[0]));
        applySort(cb, dataQuery, root, pageable.getSort());

        TypedQuery<BinView> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<BinView> content = typedQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    private long countMatches(CriteriaBuilder cb, BinSearchCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<BinEntity> countRoot = countQuery.from(BinEntity.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(toPredicates(cb, countQuery, countRoot, criteria).toArray(new Predicate[0]));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> toPredicates(CriteriaBuilder cb, CriteriaQuery<?> query, Root<BinEntity> root, BinSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(criteria.merchantId())) {
            predicates.add(merchantScopePredicate(cb, query, root, criteria.merchantId()));
        }
        if (StringUtils.hasText(criteria.zoneId())) {
            predicates.add(cb.equal(root.get(BinEntity_.ZONE_ID), criteria.zoneId()));
        }
        if (StringUtils.hasText(criteria.query())) {
            String pattern = "%" + criteria.query().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get(BinEntity_.CODE)), pattern),
                    cb.like(cb.lower(root.get(BinEntity_.NAME)), pattern)
            ));
        }
        if (criteria.active() != null) {
            predicates.add(cb.equal(root.get(BinEntity_.ACTIVE), criteria.active()));
        }

        return predicates;
    }

    private Predicate merchantScopePredicate(CriteriaBuilder cb, CriteriaQuery<?> query, Root<BinEntity> root, String merchantId) {
        Subquery<Long> scopeSubquery = query.subquery(Long.class);
        Root<ZoneEntity> zone = scopeSubquery.from(ZoneEntity.class);
        Root<LocationEntity> location = scopeSubquery.from(LocationEntity.class);
        scopeSubquery.select(cb.literal(1L));
        scopeSubquery.where(
                cb.equal(zone.get(ZoneEntity_.UUID), root.get(BinEntity_.ZONE_ID)),
                cb.equal(location.get(LocationEntity_.UUID), zone.get(ZoneEntity_.LOCATION_ID)),
                cb.equal(location.get(LocationEntity_.MERCHANT_ID), merchantId)
        );
        return cb.exists(scopeSubquery);
    }

    private void applySort(CriteriaBuilder cb, CriteriaQuery<?> query, Root<BinEntity> root, Sort sort) {
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
