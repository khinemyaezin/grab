package com.grab.discovery.adapter.persistence.specification.jpa;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.entity.DiscoveryProductVariantEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiscoveryProductSearchSpecification {

    private static final String ACTIVE_STATUS = "ACTIVE";

    public Specification<DiscoveryProductEntity> visibleForSalesChannel(
            String salesChannelId,
            String keyword,
            String categoryId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("salesChannelId"), salesChannelId));
            predicates.add(cb.isTrue(root.get("published")));
            predicates.add(cb.isTrue(root.get("channelEnabled")));
            predicates.add(cb.equal(root.get("productStatus"), ACTIVE_STATUS));
            Subquery<Long> activeVariant = query.subquery(Long.class);
            Root<DiscoveryProductVariantEntity> variantRoot = activeVariant.from(DiscoveryProductVariantEntity.class);
            activeVariant.select(cb.literal(1L));
            activeVariant.where(
                    cb.equal(variantRoot.get("discoveryProduct").get("id"), root.get("id")),
                    cb.equal(variantRoot.get("variantStatus"), ACTIVE_STATUS)
            );
            predicates.add(cb.exists(activeVariant));
            if (keyword != null && !keyword.isBlank()) {
                String escapedKeyword = escapeLike(keyword.toLowerCase(Locale.ROOT));
                String pattern = "%" + escapedKeyword + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern, '\\'),
                        cb.like(cb.lower(root.get("description")), pattern, '\\')
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
