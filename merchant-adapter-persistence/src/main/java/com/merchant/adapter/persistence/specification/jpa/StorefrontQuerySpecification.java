package com.merchant.adapter.persistence.specification.jpa;

import com.merchant.adapter.persistence.entity.StorefrontChannelBrandEntity;
import com.merchant.adapter.persistence.entity.StorefrontEntity;
import com.merchant.application.model.read.StorefrontQueryCriteria;
import com.merchant.application.model.read.StorefrontView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StorefrontQuerySpecification {
    private final EntityManager entityManager;

    public StorefrontQuerySpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<StorefrontView> findById(String storefrontId) {
        if (!StringUtils.hasLength(storefrontId)) {
            return Optional.empty();
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StorefrontView> query = cb.createQuery(StorefrontView.class);
        Root<StorefrontEntity> storefront = query.from(StorefrontEntity.class);
        Join<StorefrontEntity, StorefrontChannelBrandEntity> brand =
                storefront.join("channelBrand", JoinType.LEFT);
        query.select(cb.construct(
                StorefrontView.class,
                storefront.get("uuid"),
                storefront.get("merchantId"),
                brand.get("salesChannelId"),
                storefront.get("name"),
                storefront.get("slug"),
                storefront.get("status"),
                storefront.get("lifecycleReason"),
                storefront.get("createdAt"),
                storefront.get("updatedAt"),
                storefront.get("version")
        ));
        query.where(cb.equal(storefront.get("uuid"), storefrontId));
        List<StorefrontView> results = entityManager.createQuery(query).setMaxResults(1).getResultList();
        return results.stream().findFirst();
    }

    public List<StorefrontView> list(StorefrontQueryCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StorefrontView> query = cb.createQuery(StorefrontView.class);
        Root<StorefrontEntity> storefront = query.from(StorefrontEntity.class);
        Join<StorefrontEntity, StorefrontChannelBrandEntity> brand =
                storefront.join("channelBrand", JoinType.LEFT);

        query.select(cb.construct(
                StorefrontView.class,
                storefront.get("uuid"),
                storefront.get("merchantId"),
                brand.get("salesChannelId"),
                storefront.get("name"),
                storefront.get("slug"),
                storefront.get("status"),
                storefront.get("lifecycleReason"),
                storefront.get("createdAt"),
                storefront.get("updatedAt"),
                storefront.get("version")
        ));
        query.where(toPredicates(cb, storefront, criteria).toArray(new Predicate[0]));
        query.orderBy(cb.desc(storefront.get("createdAt")));
        return entityManager.createQuery(query).getResultList();
    }

    private List<Predicate> toPredicates(
            CriteriaBuilder cb,
            Root<StorefrontEntity> storefront,
            StorefrontQueryCriteria criteria
    ) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasLength(criteria.merchantId())) {
            predicates.add(cb.equal(storefront.get("merchantId"), criteria.merchantId()));
        }
        return predicates;
    }
}
