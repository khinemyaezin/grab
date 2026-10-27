package com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.config;

import com.grab.discovery.application.exception.StorefrontQueryServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorefrontQueryRequestInterceptorTest {
    private final StorefrontQueryRequestInterceptor interceptor = new StorefrontQueryRequestInterceptor();

    @Test
    void preHandle_missingHeader_returnsGenericInvalidKeyError() {
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(StorefrontQueryServiceException.class)
                .hasMessage("Invalid or revoked publishable key");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
    }

    @Test
    void preHandle_legacyChannelParameterRejectsBeforeHeaderValidation() {
        MockHttpServletRequest request = request();
        request.addParameter("salesChannelId", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(StorefrontQueryServiceException.class)
                .hasMessage("Query parameter salesChannelId is not supported; use X-Publishable-Key.");
    }

    @Test
    void preHandle_legacyCredentialParameterRejects() {
        MockHttpServletRequest request = request();
        request.addParameter("publishableKey", "pk_test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(StorefrontQueryServiceException.class)
                .hasMessage("Query parameter publishableKey is not supported; use X-Publishable-Key.");
    }

    @Test
    void preHandle_oneNonblankHeaderAllowsRequest() throws Exception {
        MockHttpServletRequest request = request();
        request.addHeader("X-Publishable-Key", "pk_test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
    }

    private MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/api/v1/storefront-query/products");
    }
}
