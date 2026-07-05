package com.identity.domain.policy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RegistrationAccessPolicyResolver {
    private final Map<String, RegistrationAccessPolicy> policies;

    public RegistrationAccessPolicyResolver(
            List<RegistrationAccessPolicy> policies) {

        this.policies = policies.stream()
                .collect(Collectors.toMap(
                        RegistrationAccessPolicy::platformCode,
                        Function.identity()));
    }

    public RegistrationAccessPolicy resolve(String platformCode) {
        return Optional.ofNullable(policies.get(platformCode))
                .orElseThrow(() ->
                        new UnsupportedOperationException(platformCode));
    }
}
