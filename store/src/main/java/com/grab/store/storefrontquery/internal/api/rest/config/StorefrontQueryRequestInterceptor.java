package com.grab.store.storefrontquery.internal.api.rest.config;

import com.grab.discovery.application.exception.StorefrontQueryServiceError;
import com.grab.discovery.application.exception.StorefrontQueryServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class StorefrontQueryRequestInterceptor implements HandlerInterceptor {
    private static final String PUBLISHABLE_KEY_HEADER = "X-Publishable-Key";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        response.setHeader("Cache-Control", "no-store");
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        rejectLegacyParameters(request);
        validatePublishableKeyHeader(request);
        return true;
    }

    private void rejectLegacyParameters(HttpServletRequest request) {
        if (request.getParameterMap().containsKey("salesChannelId")) {
            throw new StorefrontQueryServiceException(
                    new StorefrontQueryServiceError.UnsupportedChannelSelector(),
                    "Query parameter salesChannelId is not supported; use X-Publishable-Key."
            );
        }
        if (request.getParameterMap().containsKey("publishableKey")) {
            throw new StorefrontQueryServiceException(
                    new StorefrontQueryServiceError.LegacyPublishableKeyQuery(),
                    "Query parameter publishableKey is not supported; use X-Publishable-Key."
            );
        }
    }

    private void validatePublishableKeyHeader(HttpServletRequest request) {
        List<String> values = headerValues(request);
        if (values.size() != 1) {
            throw invalidKey();
        }
        String value = values.getFirst();
        if (value == null || value.isBlank() || value.contains(",")) {
            throw invalidKey();
        }
    }

    private List<String> headerValues(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(PUBLISHABLE_KEY_HEADER);
        List<String> values = new ArrayList<>();
        while (headers.hasMoreElements()) {
            values.add(headers.nextElement());
        }
        return values;
    }

    private StorefrontQueryServiceException invalidKey() {
        return new StorefrontQueryServiceException(
                new StorefrontQueryServiceError.InvalidPublishableKey(),
                "Invalid or revoked publishable key"
        );
    }
}
