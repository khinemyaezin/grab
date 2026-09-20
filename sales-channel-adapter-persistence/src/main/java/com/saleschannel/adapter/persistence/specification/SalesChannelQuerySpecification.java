package com.saleschannel.adapter.persistence.specification;

import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import com.saleschannel.application.model.read.SalesChannelQueryCriteria;
import com.saleschannel.application.model.read.SalesChannelView;
import com.saleschannel.domain.enums.ChannelType;
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
import java.util.Optional;

public class SalesChannelQuerySpecification {
    private final EntityManager entityManager;

    public SalesChannelQuerySpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<SalesChannelView> list(SalesChannelQueryCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        long total = countMatches(cb, criteria);
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        CriteriaQuery<SalesChannelView> dataQuery = cb.createQuery(SalesChannelView.class);
        Root<SalesChannelEntity> channel = dataQuery.from(SalesChannelEntity.class);
        dataQuery.select(cb.construct(
                SalesChannelView.class,
                channel.get("uuid"),
                channel.get("name"),
                channel.get("type"),
                channel.get("owner"),
                channel.get("merchantId"),
                channel.get("status"),
                channel.get("createdAt"),
                channel.get("updatedAt"),
                channel.get("version")
        ));
        dataQuery.where(toPredicates(cb, channel, criteria).toArray(new Predicate[0]));
        applySort(cb, dataQuery, channel, pageable.getSort());

        TypedQuery<SalesChannelView> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<SalesChannelView> content = typedQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<SalesChannelView> findById(String id) {
        if (!StringUtils.hasLength(id)) {
            return Optional.empty();
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SalesChannelView> dataQuery = cb.createQuery(SalesChannelView.class);
        Root<SalesChannelEntity> channel = dataQuery.from(SalesChannelEntity.class);
        dataQuery.select(cb.construct(
                SalesChannelView.class,
                channel.get("uuid"),
                channel.get("name"),
                channel.get("type"),
                channel.get("owner"),
                channel.get("merchantId"),
                channel.get("status"),
                channel.get("createdAt"),
                channel.get("updatedAt"),
                channel.get("version")
        ));
        dataQuery.where(cb.equal(channel.get("uuid"), id));
        List<SalesChannelView> results = entityManager.createQuery(dataQuery)
                .setMaxResults(1)
                .getResultList();
        return results.stream().findFirst();
    }

    private long countMatches(CriteriaBuilder cb, SalesChannelQueryCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SalesChannelEntity> countRoot = countQuery.from(SalesChannelEntity.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(toPredicates(cb, countRoot, criteria).toArray(new Predicate[0]));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private void applySort(
            CriteriaBuilder cb,
            CriteriaQuery<?> query,
            Root<SalesChannelEntity> channel,
            Sort sort
    ) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(cb.asc(channel.get("type")), cb.asc(channel.get("createdAt")));
            return;
        }
        List<Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Path<Object> path = channel.get(order.getProperty());
            orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
        }
        query.orderBy(orders);
    }

    private List<Predicate> toPredicates(
            CriteriaBuilder cb,
            Root<SalesChannelEntity> channel,
            SalesChannelQueryCriteria criteria
    ) {
        List<Predicate> predicates = new ArrayList<>();
        Predicate marketplace = cb.equal(channel.get("type"), ChannelType.MARKETPLACE);
        if (StringUtils.hasLength(criteria.merchantId())) {
            Predicate website = cb.and(
                    cb.equal(channel.get("type"), ChannelType.WEBSITE),
                    cb.equal(channel.get("merchantId"), criteria.merchantId())
            );
            predicates.add(cb.or(marketplace, website));
            return predicates;
        }
        predicates.add(marketplace);
        return predicates;
    }
}
