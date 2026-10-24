package com.saleschannel.adapter.persistence.specification.jpa;

import com.saleschannel.adapter.persistence.entity.PublishableKeyJpaEntity;
import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import com.saleschannel.adapter.persistence.entity.meta.PublishableKeyJpaEntity_;
import com.saleschannel.adapter.persistence.entity.meta.SalesChannelEntity_;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public class PublishableKeyQuerySpecification {
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "keyId", PublishableKeyJpaEntity_.UUID,
            PublishableKeyJpaEntity_.LABEL, PublishableKeyJpaEntity_.LABEL,
            PublishableKeyJpaEntity_.CREATED_AT, PublishableKeyJpaEntity_.CREATED_AT,
            PublishableKeyJpaEntity_.REVOKED_AT, PublishableKeyJpaEntity_.REVOKED_AT);
    private final EntityManager entityManager;

    public PublishableKeyQuerySpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<PublishableKeyView> list(String salesChannelId, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<PublishableKeyJpaEntity> countRoot = countQuery.from(PublishableKeyJpaEntity.class);
        Expression<Long> countExpression = builder.count(countRoot);
        countQuery.select(countExpression);
        Path<String> countChannelId = countRoot.get(PublishableKeyJpaEntity_.SALES_CHANNEL_ID);
        Predicate channelFilter = builder.equal(countChannelId, salesChannelId);
        countQuery.where(channelFilter);
        TypedQuery<Long> count = entityManager.createQuery(countQuery);
        long total = count.getSingleResult();

        CriteriaQuery<PublishableKeyView> query = builder.createQuery(PublishableKeyView.class);
        Root<PublishableKeyJpaEntity> key = query.from(PublishableKeyJpaEntity.class);
        Path<String> keyId = key.get(PublishableKeyJpaEntity_.UUID);
        Path<String> channelId = key.get(PublishableKeyJpaEntity_.SALES_CHANNEL_ID);
        Path<String> label = key.get(PublishableKeyJpaEntity_.LABEL);
        Path<String> prefix = key.get(PublishableKeyJpaEntity_.DISPLAY_PREFIX);
        Path<Instant> createdAt = key.get(PublishableKeyJpaEntity_.CREATED_AT);
        Path<Instant> revokedAt = key.get(PublishableKeyJpaEntity_.REVOKED_AT);
        CompoundSelection<PublishableKeyView> selection = builder.construct(
                PublishableKeyView.class, keyId, channelId, label, prefix, createdAt, revokedAt);
        query.select(selection);
        Predicate filter = builder.equal(channelId, salesChannelId);
        query.where(filter);
        Sort sort = pageable.getSort();
        applySort(builder, query, key, sort);
        TypedQuery<PublishableKeyView> data = entityManager.createQuery(query);
        if (pageable.isPaged()) {
            long requestedOffset = pageable.getOffset();
            int offset = Math.toIntExact(requestedOffset);
            int pageSize = pageable.getPageSize();
            data.setFirstResult(offset);
            data.setMaxResults(pageSize);
        }
        List<PublishableKeyView> content = data.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<ResolvedPublishableKeyView> findActiveByDigest(String digest) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ResolvedPublishableKeyView> query = builder.createQuery(ResolvedPublishableKeyView.class);
        Root<PublishableKeyJpaEntity> key = query.from(PublishableKeyJpaEntity.class);
        Root<SalesChannelEntity> channel = query.from(SalesChannelEntity.class);
        Path<String> channelId = channel.get(SalesChannelEntity_.UUID);
        Path<ChannelStatus> channelStatus = channel.get(SalesChannelEntity_.STATUS);
        Predicate channelEnabled = builder.equal(channelStatus, ChannelStatus.ENABLED);
        Expression<Boolean> keyActive = builder.literal(true);
        CompoundSelection<ResolvedPublishableKeyView> selection = builder.construct(
                ResolvedPublishableKeyView.class, channelId, channelEnabled, keyActive);
        query.select(selection);
        Path<String> tokenDigest = key.get(PublishableKeyJpaEntity_.TOKEN_DIGEST);
        Path<Instant> revokedAt = key.get(PublishableKeyJpaEntity_.REVOKED_AT);
        Path<String> keyChannelId = key.get(PublishableKeyJpaEntity_.SALES_CHANNEL_ID);
        Predicate matchingDigest = builder.equal(tokenDigest, digest);
        Predicate active = builder.isNull(revokedAt);
        Predicate matchingChannel = builder.equal(keyChannelId, channelId);
        query.where(matchingDigest, active, matchingChannel);
        TypedQuery<ResolvedPublishableKeyView> data = entityManager.createQuery(query);
        data.setMaxResults(1);
        List<ResolvedPublishableKeyView> content = data.getResultList();
        return content.stream().findFirst();
    }

    private void applySort(CriteriaBuilder builder, CriteriaQuery<PublishableKeyView> query,
                           Root<PublishableKeyJpaEntity> key, Sort sort) {
        List<Order> orders = new ArrayList<>();
        if (sort.isUnsorted()) {
            Path<Instant> createdAt = key.get(PublishableKeyJpaEntity_.CREATED_AT);
            Order newestFirst = builder.desc(createdAt);
            orders.add(newestFirst);
        }
        for (Sort.Order order : sort) {
            String field = order.getProperty();
            String property = SORT_FIELDS.get(field);
            if (property == null) {
                throw new IllegalArgumentException("Unsupported publishable key sort field");
            }
            Path<Object> path = key.get(property);
            Order ordering = order.isAscending() ? builder.asc(path) : builder.desc(path);
            orders.add(ordering);
        }
        Path<String> keyId = key.get(PublishableKeyJpaEntity_.UUID);
        Order tieBreaker = builder.asc(keyId);
        orders.add(tieBreaker);
        query.orderBy(orders);
    }

}
