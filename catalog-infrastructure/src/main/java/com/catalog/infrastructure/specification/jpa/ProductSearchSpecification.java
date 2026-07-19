package com.catalog.infrastructure.specification.jpa;

import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.meta.ProductEntity_;
import com.catalog.infrastructure.entity.meta.ProductVariantEntity_;
import com.catalog.infrastructure.view.ProductView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
                product.get(ProductEntity_.CATEGORY_ENTITY)
        ));
        dataQuery.where(toPredicates(cb, dataQuery, product, criteria).toArray(new Predicate[0]));
        dataQuery.orderBy(cb.asc(product.get(ProductEntity_.ID)));

        TypedQuery<ProductView> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<ProductView> content = typedQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
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

        predicates.add(cb.equal(product.get(ProductEntity_.MERCHANT_ID), criteria.merchantId()));

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
}
