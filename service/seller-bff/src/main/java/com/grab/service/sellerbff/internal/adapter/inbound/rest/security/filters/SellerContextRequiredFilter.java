package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.filters;

import com.grab.framework.security.AccessContext;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.exception.SellerContextRequiredException;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.merchant.application.security.MerchantScopeManifest.ACCOUNT_SCOPE_KEY;
import static com.merchant.application.security.MerchantScopeManifest.STOREFRONT_SCOPE_KEY;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

public final class SellerContextRequiredFilter extends GenericFilterBean {
    private static final RequestMatcher MERCHANT_ROOT = reads("/api/v1/merchants/root");
    private static final RequestMatcher ACCOUNT = new OrRequestMatcher(
            reads("/api/v1/products",
                    "/api/v1/products/{productId}",
                    "/api/v1/products/by-slug/{slug}",
                    "/api/v1/products/{productId}/variants/{variantId}",
                    "/api/v1/inventory",
                    "/api/v1/inventory/reorder-suggestions",
                    "/api/v1/inventory/items/{itemId}",
                    "/api/v1/inventory/items/{itemId}/movements/latest",
                    "/api/v1/facilities/locations/{locationId}",
                    "/api/v1/facilities/zones/{zoneId}",
                    "/api/v1/facilities/bins/{binId}",
                    "/api/v1/pricing",
                    "/api/v1/pricing/preferences/{preferenceId}",
                    "/api/v1/pricing/price-lists",
                    "/api/v1/pricing/price-lists/{priceListId}",
                    "/api/v1/pricing/price-sets/{priceSetId}",
                    "/api/v1/sales-channels",
                    "/api/v1/sales-channels/root",
                    "/api/v1/sales-channels/{salesChannelId}",
                    "/api/v1/sales-channels/publishable-keys/resolve"),
            paths(POST, "/api/v1/products",
                    "/api/v1/inventory/items",
                    "/api/v1/inventory/allocations",
                    "/api/v1/inventory/allocations/deallocate",
                    "/api/v1/facilities/locations",
                    "/api/v1/facilities/locations/{locationId}/zones",
                    "/api/v1/facilities/zones/{zoneId}/bins",
                    "/api/v1/pricing/preferences",
                    "/api/v1/pricing/price-lists",
                    "/api/v1/pricing/price-sets",
                    "/api/v1/pricing/price-sets/calculate",
                    "/api/v1/sales-channels/merchant-website",
                    "/api/v1/sales-channels/{channelId}/publishable-keys"),
            paths(PUT, "/api/v1/products/{productId}"),
            paths(DELETE, "/api/v1/products/{productId}",
                    "/api/v1/facilities/locations/{locationId}",
                    "/api/v1/facilities/zones/{zoneId}",
                    "/api/v1/facilities/bins/{binId}",
                    "/api/v1/sales-channels/{channelId}/publishable-keys/{keyId}"),
            paths(PATCH, "/api/v1/products/{productId}/status",
                    "/api/v1/facilities/locations/{locationId}/activate",
                    "/api/v1/facilities/locations/{locationId}/deactivate",
                    "/api/v1/facilities/zones/{zoneId}/activate",
                    "/api/v1/facilities/zones/{zoneId}/deactivate",
                    "/api/v1/facilities/bins/{binId}/activate",
                    "/api/v1/facilities/bins/{binId}/deactivate"));

    private static final RequestMatcher SELECTED = new OrRequestMatcher(
            reads("/api/v1/categories/tree",
                    "/api/v1/categories/leaf-nodes",
                    "/api/v1/categories/{categoryId}",
                    "/api/v1/categories/{categoryId}/children",
                    "/api/v1/variant-options",
                    "/api/v1/variant-options/types",
                    "/api/v1/identity/admin/merchants"),
            paths(POST, "/api/v1/identity/admin/merchants/{merchantId}/request-changes",
                    "/api/v1/identity/admin/merchants/{merchantId}/approve",
                    "/api/v1/identity/admin/merchants/{merchantId}/reject",
                    "/api/v1/identity/admin/merchants/{merchantId}/suspend",
                    "/api/v1/identity/admin/merchants/{merchantId}/reactivate",
                    "/api/v1/identity/admin/merchants/{merchantId}/close"));

    private static final RequestMatcher PLATFORM = new OrRequestMatcher(
            paths(POST, "/api/v1/categories"),
            paths(DELETE, "/api/v1/categories/{categoryId}"));

    private static final RequestMatcher MERCHANT_ID = new OrRequestMatcher(
            reads("/api/v1/merchants/{merchantId}",
                    "/api/v1/merchants/{merchantId}/members",
                    "/api/v1/merchants/{merchantId}/storefronts"),
            paths(PATCH, "/api/v1/merchants/{merchantId}/profile",
                    "/api/v1/merchants/{merchantId}/members/{memberId}/role"),
            paths(POST, "/api/v1/merchants/{merchantId}/members/invite",
                    "/api/v1/merchants/{merchantId}/members/{memberId}/accept",
                    "/api/v1/merchants/{merchantId}/storefronts"),
            paths(DELETE, "/api/v1/merchants/{merchantId}/members/{memberId}"));

    private static final RequestMatcher MERCHANT_OR_STOREFRONT_ID = new OrRequestMatcher(
            reads("/api/v1/merchants/{merchantId}/storefronts/{storefrontId}"),
            paths(PUT, "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}"),
            paths(PATCH, "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}/activate",
                    "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}/close"));

    private static RequestMatcher reads(String... patterns) {
        return new OrRequestMatcher(paths(GET, patterns), paths(HEAD, patterns));
    }

    private static RequestMatcher paths(HttpMethod method, String... patterns) {
        var builder = PathPatternRequestMatcher.withDefaults();
        List<RequestMatcher> matchers = Arrays.stream(patterns)
                .<RequestMatcher>map(pattern -> builder.matcher(method, pattern))
                .toList();
        return new OrRequestMatcher(matchers);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (!MERCHANT_ROOT.matches(httpRequest)) {
            verifyContext(httpRequest);
        }
        chain.doFilter(request, response);
    }

    private static void verifyContext(HttpServletRequest request) {
        var merchant = MERCHANT_ID.matcher(request);
        var storefront = MERCHANT_OR_STOREFRONT_ID.matcher(request);
        if (merchant.isMatch() || storefront.isMatch()) {
            AccessContext context = requiredContext();
            var variables = merchant.isMatch() ? merchant.getVariables() : storefront.getVariables();
            boolean ownsMerchant = ACCOUNT_SCOPE_KEY.equals(context.scopeKey())
                    && variables.get("merchantId").equals(context.scopeId());
            boolean ownsStorefront = storefront.isMatch() && STOREFRONT_SCOPE_KEY.equals(context.scopeKey())
                    && variables.get("storefrontId").equals(context.scopeId());
            if (!ownsMerchant && !ownsStorefront) {
                throw new AccessDeniedException("Seller context does not match the requested resource");
            }
        } else if (ACCOUNT.matches(request)) {
            requireScope(ACCOUNT_SCOPE_KEY);
        } else if (PLATFORM.matches(request)) {
            requireScope("platform");
        } else if (SELECTED.matches(request)) {
            requiredContext();
        }
    }

    private static void requireScope(String key) {
        AccessContext context = requiredContext();
        if (!key.equals(context.scopeKey())) {
            throw new AccessDeniedException("Seller context does not match this endpoint");
        }
    }

    private static AccessContext requiredContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SellerSecurityPrincipal principal)) {
            throw new AccessDeniedException("Live seller identity required");
        }
        return principal.getAccessContext().orElseThrow(SellerContextRequiredException::new);
    }
}
