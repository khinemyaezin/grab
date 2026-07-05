package com.identity.domain.policy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccessPlacementPolicyResolver {
    private final Map<String, AccessPlacementPolicy> policies;

    public AccessPlacementPolicyResolver(List<AccessPlacementPolicy> policies) {
        this.policies = policies.stream()
                .collect(Collectors.toMap(
                        AccessPlacementPolicy::placementRoleCode,
                        Function.identity()));
    }

    public AccessPlacementPolicy resolve(String placementCode) {
        return policies.get(placementCode);
    }
}
