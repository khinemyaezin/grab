package com.grab.discovery.adapter.persistence.specification.jpa;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryProductSearchSpecification {

    private static final String ACTIVE_STATUS = "ACTIVE";

    public Specification<DiscoveryProductEntity> visibleForSalesChannel(String salesChannelId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("salesChannelId"), salesChannelId));
            predicates.add(cb.isTrue(root.get("published")));
            predicates.add(cb.isTrue(root.get("channelEnabled")));
            predicates.add(cb.equal(root.get("productStatus"), ACTIVE_STATUS));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
