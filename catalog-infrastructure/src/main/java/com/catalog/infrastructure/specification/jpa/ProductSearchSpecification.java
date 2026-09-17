package com.catalog.infrastructure.specification.jpa;

import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.infrastructure.entity.entity.CatalogMerchantAvailabilityEntity;
import com.catalog.infrastructure.entity.entity.MediaEntity;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.meta.ProductEntity_;
import com.catalog.infrastructure.entity.meta.ProductVariantEntity_;
import com.catalog.infrastructure.view.ProductHeroMediaView;
import com.catalog.infrastructure.view.ProductPublicationView;
import com.catalog.infrastructure.view.ProductVariantRefView;
import com.catalog.infrastructure.view.ProductView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductSearchSpecification {

    private final EntityManager entityManager;

    public ProductSearchSpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<ProductView> search(ProductSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        long total = countMatches(cb, criteria);
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        CriteriaQuery<ProductView> dataQuery = cb.createQuery(ProductView.class);
        Root<ProductEntity> product = dataQuery.from(ProductEntity.class);

        dataQuery.select(cb.construct(
                ProductView.class,
                product.get(ProductEntity_.UUID),
                product.get(ProductEntity_.NAME),
                product.get(ProductEntity_.STATUS),
                product.get(ProductEntity_.SLUG),
                product.get(ProductEntity_.CATEGORY_ENTITY),
                product.get(ProductEntity_.MERCHANT_ID),
                product.get(ProductEntity_.FEATURED),
                product.get(ProductEntity_.LISTING_CONDITION)
        ));
        dataQuery.where(toPredicates(cb, dataQuery, product, criteria).toArray(new Predicate[0]));
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : pageable.getSort()) {
                orders.add(order.isAscending()
                        ? cb.asc(product.get(order.getProperty()))
                        : cb.desc(product.get(order.getProperty())));
            }
            dataQuery.orderBy(orders);
        } else {
            dataQuery.orderBy(cb.desc(product.get(ProductEntity_.UPDATED_AT)));
        }

        TypedQuery<ProductView> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<ProductView> content = typedQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    public List<ProductHeroMediaView> findHeroMediasByProductIds(Collection<String> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductHeroMediaView> query = cb.createQuery(ProductHeroMediaView.class);
        Root<ProductEntity> product = query.from(ProductEntity.class);
        Join<ProductEntity, MediaEntity> media = product.join(ProductEntity_.MEDIA_ENTITIES);

        query.select(cb.construct(
                ProductHeroMediaView.class,
                product.get(ProductEntity_.UUID),
                media.get("uuid"),
                cb.coalesce(media.get("storageKey"), media.get("path")),
                cb.coalesce(media.get("contentType"), media.get("type")),
                media.get("rank")
        ));
        query.where(
                product.get(ProductEntity_.UUID).in(productIds),
                cb.equal(media.get("rank"), 0)
        );

        return entityManager.createQuery(query).getResultList();
    }

    public List<ProductVariantRefView> findActiveVariantsByProductIds(Collection<String> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return List.of();
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductVariantRefView> query = cb.createQuery(ProductVariantRefView.class);
        Root<ProductVariantEntity> variant = query.from(ProductVariantEntity.class);
        Join<ProductVariantEntity, ProductEntity> product = variant.join(ProductVariantEntity_.PRODUCT);
        query.select(cb.construct(
                ProductVariantRefView.class,
                product.get(ProductEntity_.UUID),
                variant.get(ProductVariantEntity_.UUID),
                variant.get(ProductVariantEntity_.SKU)
        ));
        query.where(
                product.get(ProductEntity_.UUID).in(productIds),
                cb.equal(variant.get(ProductVariantEntity_.STATUS), "ACTIVE")
        );
        return entityManager.createQuery(query).getResultList();
    }

    public List<ProductPublicationView> findPublicationsByProductIds(Collection<String> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return List.of();
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductPublicationView> query = cb.createQuery(ProductPublicationView.class);
        Root<ProductPublicationEntity> publication = query.from(ProductPublicationEntity.class);
        Root<ProductEntity> product = query.from(ProductEntity.class);
        query.select(cb.construct(
                ProductPublicationView.class,
                product.get(ProductEntity_.UUID),
                publication.get("salesChannelId")
        ));
        query.where(
                cb.equal(product.get(ProductEntity_.ID), publication.get("productId")),
                product.get(ProductEntity_.UUID).in(productIds)
        );
        return entityManager.createQuery(query).getResultList();
    }

    private long countMatches(CriteriaBuilder cb, ProductSearchCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProductEntity> product = countQuery.from(ProductEntity.class);

        countQuery.select(cb.count(product));
        countQuery.where(toPredicates(cb, countQuery, product, criteria).toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> toPredicates(
            CriteriaBuilder cb,
            CriteriaQuery<?> query,
            Root<ProductEntity> product,
            ProductSearchCriteria criteria
    ) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasLength(criteria.merchantId())) {
            predicates.add(cb.equal(product.get(ProductEntity_.MERCHANT_ID), criteria.merchantId()));
        }

        if (criteria.storefrontVisible()) {
            predicates.add(cb.equal(product.get(ProductEntity_.STATUS), ProductStatus.ACTIVE));
            predicates.add(hasActiveMerchant(cb, query, product));
            predicates.add(hasVariantWithStatus(cb, query, product, "ACTIVE"));
        }

        if (StringUtils.hasLength(criteria.salesChannelId())) {
            predicates.add(isPublishedToChannel(cb, query, product, criteria.salesChannelId()));
        }

        if (criteria.featured() != null) {
            predicates.add(cb.equal(product.get(ProductEntity_.FEATURED), criteria.featured()));
        }

        if (StringUtils.hasLength(criteria.condition())) {
            predicates.add(cb.equal(
                    product.get(ProductEntity_.LISTING_CONDITION),
                    criteria.condition().toUpperCase()
            ));
        }

        if (StringUtils.hasLength(criteria.query())) {
            String pattern = "%" + criteria.query().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(product.get(ProductEntity_.NAME)), pattern),
                    hasVariantSkuLike(cb, query, product, pattern)
            ));
        }

        if (StringUtils.hasLength(criteria.categoryId())) {
            predicates.add(cb.equal(product.get(ProductEntity_.CATEGORY_ENTITY), criteria.categoryId()));
        }

        if (StringUtils.hasLength(criteria.productStatus())) {
            predicates.add(cb.equal(
                    product.get(ProductEntity_.STATUS),
                    ProductStatus.valueOf(criteria.productStatus().toUpperCase())
            ));
        }

        if (StringUtils.hasLength(criteria.variantStatus())) {
            predicates.add(hasVariantWithStatus(cb, query, product, criteria.variantStatus()));
        }

        return predicates;
    }

    private Predicate hasVariantSkuLike(
            CriteriaBuilder cb,
            CriteriaQuery<?> query,
            Root<ProductEntity> product,
            String pattern
    ) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<ProductVariantEntity> variant = subquery.from(ProductVariantEntity.class);
        subquery.select(cb.literal(1));
        subquery.where(
                cb.equal(variant.get(ProductVariantEntity_.PRODUCT), product),
                cb.like(cb.lower(variant.get(ProductVariantEntity_.SKU)), pattern)
        );
        return cb.exists(subquery);
    }

    private Predicate hasVariantWithStatus(
            CriteriaBuilder cb,
            CriteriaQuery<?> query,
            Root<ProductEntity> product,
            String variantStatus
    ) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<ProductVariantEntity> variant = subquery.from(ProductVariantEntity.class);
        subquery.select(cb.literal(1));
        subquery.where(
                cb.equal(variant.get(ProductVariantEntity_.PRODUCT), product),
                cb.equal(variant.get(ProductVariantEntity_.STATUS), variantStatus)
        );
        return cb.exists(subquery);
    }

    private Predicate hasActiveMerchant(
            CriteriaBuilder cb,
            CriteriaQuery<?> query,
            Root<ProductEntity> product
    ) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<CatalogMerchantAvailabilityEntity> availability = subquery.from(CatalogMerchantAvailabilityEntity.class);
        subquery.select(cb.literal(1));
        subquery.where(
                cb.equal(availability.get("merchantId"), product.get(ProductEntity_.MERCHANT_ID)),
                cb.equal(availability.get("status"), "ACTIVE")
        );
        return cb.exists(subquery);
    }

    private Predicate isPublishedToChannel(
            CriteriaBuilder cb,
            CriteriaQuery<?> query,
            Root<ProductEntity> product,
            String salesChannelId
    ) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<ProductPublicationEntity> publication = subquery.from(ProductPublicationEntity.class);
        subquery.select(cb.literal(1));
        subquery.where(
                cb.equal(publication.get("productId"), product.get(ProductEntity_.ID)),
                cb.equal(publication.get("salesChannelId"), salesChannelId)
        );
        return cb.exists(subquery);
    }
}
