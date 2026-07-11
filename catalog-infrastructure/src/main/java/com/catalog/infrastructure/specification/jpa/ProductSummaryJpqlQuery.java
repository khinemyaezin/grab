package com.catalog.infrastructure.specification.jpa;

import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.meta.ProductEntity_;
import com.catalog.infrastructure.entity.meta.ProductVariantEntity_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProductSummaryJpqlQuery {

    private ProductSummaryJpqlQuery() {
    }

    public static Page<ProductEntity> search(
            EntityManager em,
            ProductSearchCriteria criteria,
            Pageable pageable
    ) {

        Map<String, Object> params = new HashMap<>();
        String whereClause = buildWhereClause(criteria, params);

        String countJpql = "SELECT COUNT(DISTINCT p) FROM ProductEntity p " +
                whereClause;

        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        params.forEach(countQuery::setParameter);

        long total = countQuery.getSingleResult();

        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        String idJpql = "SELECT p.id FROM ProductEntity p " +
                whereClause + " ORDER BY p.id";

        TypedQuery<Long> idQuery = em.createQuery(idJpql, Long.class);
        params.forEach(idQuery::setParameter);

        idQuery.setFirstResult((int) pageable.getOffset());
        idQuery.setMaxResults(pageable.getPageSize());

        List<Long> productIds = idQuery.getResultList();

        if (productIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<ProductEntity> products = em.createQuery(
                "SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.productVariants WHERE p.id IN :ids",
                        ProductEntity.class)
                .setParameter("ids", productIds)
                .getResultList();

        em.createQuery(
                "SELECT DISTINCT pv FROM ProductVariantEntity pv LEFT JOIN FETCH pv.productVariations WHERE pv.product IN :products",
                        ProductVariantEntity.class)
                .setParameter("products", products)
                .getResultList();

        Map<Long, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        List<ProductEntity> orderedProducts = productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(orderedProducts, pageable, total);
    }

    private static String buildWhereClause(
            ProductSearchCriteria criteria,
            Map<String, Object> params
    ) {

        List<String> conditions = new ArrayList<>();

        if (StringUtils.hasLength(criteria.merchantId())) {
            conditions.add("p." + ProductEntity_.MERCHANT_ID + " = :merchantId");
            params.put("merchantId", criteria.merchantId());
        }

        if (StringUtils.hasLength(criteria.productName())) {
            conditions.add("LOWER(p." + ProductEntity_.NAME + ") LIKE :productName");
            params.put("productName", "%" + criteria.productName().toLowerCase() + "%");
        }

        if(StringUtils.hasLength(criteria.categoryId())) {
            conditions.add("p." + ProductEntity_.CATEGORY_ENTITY + " = :categoryId");
            params.put("categoryId", criteria.categoryId());
        }

        if(StringUtils.hasLength(criteria.productStatus())) {
            conditions.add("p." + ProductEntity_.STATUS + " = :productStatus");
            params.put("productStatus", ProductStatus.valueOf(criteria.productStatus().toUpperCase()));
        }

        if (StringUtils.hasLength(criteria.sku())) {
            conditions.add(
                    "EXISTS (" +
                    "SELECT v." + ProductVariantEntity_.ID + " FROM ProductVariantEntity v " +
                    "WHERE v." + ProductVariantEntity_.PRODUCT + " = p " +
                    "AND LOWER(v." + ProductVariantEntity_.SKU + ") LIKE :sku" +
                    ")");
            params.put("sku", "%" + criteria.sku().toLowerCase() + "%");
        }

        if (StringUtils.hasLength(criteria.variantStatus())) {
            conditions.add(
                    "EXISTS (" +
                    "SELECT v." + ProductVariantEntity_.ID + " FROM ProductVariantEntity v " +
                    "WHERE v." + ProductVariantEntity_.PRODUCT + " = p " +
                    "AND v." + ProductVariantEntity_.STATUS + " = :variantStatus" +
                    ")");
            params.put("variantStatus", criteria.variantStatus());
        }

        if (conditions.isEmpty()) {
            return "";
        }

        return " WHERE " + String.join(" AND ", conditions);
    }
}
