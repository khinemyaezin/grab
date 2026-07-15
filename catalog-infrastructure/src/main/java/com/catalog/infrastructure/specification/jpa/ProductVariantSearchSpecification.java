package com.catalog.infrastructure.specification.jpa;

import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.meta.ProductEntity_;
import com.catalog.infrastructure.entity.meta.ProductVariantEntity_;
import com.catalog.infrastructure.view.ProductVariantView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductVariantSearchSpecification {

    private final EntityManager entityManager;

    public ProductVariantSearchSpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<ProductVariantView> search(ProductSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        long total = countMatches(cb, criteria);
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        CriteriaQuery<ProductVariantView> dataQuery = cb.createQuery(ProductVariantView.class);
        Root<ProductVariantEntity> variant = dataQuery.from(ProductVariantEntity.class);
        Join<ProductVariantEntity, ProductEntity> product = variant.join(ProductVariantEntity_.PRODUCT);

        dataQuery.select(cb.construct(
                ProductVariantView.class,
                product.get(ProductEntity_.UUID),
                variant.get(ProductVariantEntity_.UUID),
                variant.get(ProductVariantEntity_.SKU),
                product.get(ProductEntity_.STATUS),
                product.get(ProductEntity_.SLUG),
                product.get(ProductEntity_.CATEGORY_ENTITY),
                product.get(ProductEntity_.NAME)
        ));
        dataQuery.where(toPredicates(cb, variant, product, criteria).toArray(new Predicate[0]));
        dataQuery.orderBy(cb.asc(variant.get(ProductVariantEntity_.ID)));

        TypedQuery<ProductVariantView> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<ProductVariantView> content = typedQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    private long countMatches(CriteriaBuilder cb, ProductSearchCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProductVariantEntity> variant = countQuery.from(ProductVariantEntity.class);
        Join<ProductVariantEntity, ProductEntity> product = variant.join(ProductVariantEntity_.PRODUCT);

        countQuery.select(cb.count(variant));
        countQuery.where(toPredicates(cb, variant, product, criteria).toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> toPredicates(
            CriteriaBuilder cb,
            Root<ProductVariantEntity> variant,
            Join<ProductVariantEntity, ProductEntity> product,
            ProductSearchCriteria criteria
    ) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(product.get(ProductEntity_.MERCHANT_ID), criteria.merchantId()));

        if (StringUtils.hasLength(criteria.query())) {
            String pattern = "%" + criteria.query().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(product.get(ProductEntity_.NAME)), pattern),
                    cb.like(cb.lower(variant.get(ProductVariantEntity_.SKU)), pattern)
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
            predicates.add(cb.equal(variant.get(ProductVariantEntity_.STATUS), criteria.variantStatus()));
        }

        return predicates;
    }
}
