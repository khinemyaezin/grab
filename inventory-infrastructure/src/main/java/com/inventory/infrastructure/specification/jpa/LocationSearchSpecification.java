package com.inventory.infrastructure.specification.jpa;

import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.meta.LocationEntity_;
import com.inventory.infrastructure.view.LocationView;
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

public class LocationSearchSpecification {

    private final EntityManager entityManager;

    public LocationSearchSpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<LocationView> search(LocationSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        long total = countMatches(cb, criteria);
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        CriteriaQuery<LocationView> dataQuery = cb.createQuery(LocationView.class);
        Root<LocationEntity> root = dataQuery.from(LocationEntity.class);
        dataQuery.select(cb.construct(
                LocationView.class,
                root.get(LocationEntity_.UUID),
                root.get(LocationEntity_.CODE),
                root.get(LocationEntity_.NAME),
                root.get(LocationEntity_.TYPE),
                root.get(LocationEntity_.STREET),
                root.get(LocationEntity_.STREET2),
                root.get(LocationEntity_.CITY),
                root.get(LocationEntity_.STATE),
                root.get(LocationEntity_.POSTAL_CODE),
                root.get(LocationEntity_.COUNTRY),
                root.get(LocationEntity_.ACTIVE)
        ));
        dataQuery.where(toPredicates(cb, root, criteria).toArray(new Predicate[0]));
        applySort(cb, dataQuery, root, pageable.getSort());

        TypedQuery<LocationView> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<LocationView> content = typedQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    private long countMatches(CriteriaBuilder cb, LocationSearchCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<LocationEntity> countRoot = countQuery.from(LocationEntity.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(toPredicates(cb, countRoot, criteria).toArray(new Predicate[0]));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> toPredicates(CriteriaBuilder cb, Root<LocationEntity> root, LocationSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(criteria.merchantId())) {
            predicates.add(cb.equal(root.get(LocationEntity_.MERCHANT_ID), criteria.merchantId()));
        }
        if (StringUtils.hasText(criteria.query())) {
            String pattern = "%" + criteria.query().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get(LocationEntity_.CODE)), pattern),
                    cb.like(cb.lower(root.get(LocationEntity_.NAME)), pattern)
            ));
        }
        if (criteria.type() != null) {
            predicates.add(cb.equal(root.get(LocationEntity_.TYPE), criteria.type()));
        }
        if (criteria.active() != null) {
            predicates.add(cb.equal(root.get(LocationEntity_.ACTIVE), criteria.active()));
        }

        return predicates;
    }

    private void applySort(CriteriaBuilder cb, CriteriaQuery<?> query, Root<LocationEntity> root, Sort sort) {
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
