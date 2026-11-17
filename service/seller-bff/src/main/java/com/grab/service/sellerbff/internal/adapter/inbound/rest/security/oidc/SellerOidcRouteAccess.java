package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import jakarta.servlet.http.HttpServletRequest;

final class SellerOidcRouteAccess {
    private SellerOidcRouteAccess() {}

    static void require(HttpServletRequest request, AuthenticatedActor actor) {
        String path = request.getServletPath();
        boolean read = "GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod());
        if (under(path, "/api/v1/identity/session") || under(path, "/api/v1/merchants/applications")
                || under(path, "/api/v1/regions") || (read && path.equals("/api/v1/merchants"))) {
            return;
        }
        AccessContext context = actor.accessContext();
        if (context == null) {
            throw SellerOidcFailure.contextRequired();
        }
        if (under(path, "/api/v1/identity/admin/merchants")) {
            requireAuthority(actor, read ? "MERCHANT_GLOBAL_READ" : "MERCHANT_LIFECYCLE_WRITE");
        } else if (under(path, "/api/v1/merchants")) {
            requireMerchant(path, actor, context, read);
        } else if (under(path, "/api/v1/sales-channels")) {
            requireMerchantContext(context);
            boolean key = path.contains("/publishable-keys");
            if (key) {
                requireAuthority(actor, read ? "SALES_CHANNEL_KEY_READ" : "SALES_CHANNEL_KEY_WRITE");
            } else {
                requireAuthority(actor, read ? "SALES_CHANNEL_READ" : "SALES_CHANNEL_WRITE");
            }
        } else if (under(path, "/api/v1/inventory") || under(path, "/api/v1/facilities")) {
            requireMerchantContext(context);
            requireAuthority(actor, read ? "INVENTORY_READ" : "INVENTORY_WRITE");
        } else if (under(path, "/api/v1/products") || under(path, "/api/v1/pricing")) {
            requireMerchantContext(context);
            requireAuthority(actor, read ? "CATALOG_READ" : "CATALOG_WRITE");
        } else if (under(path, "/api/v1/categories") || under(path, "/api/v1/variant-options")) {
            requireAuthority(actor, read ? "CATALOG_READ" : "CATALOG_WRITE");
            if (!read && !"platform".equals(context.scopeKey())) {
                throw SellerOidcFailure.forbidden();
            }
        } else {
            throw SellerOidcFailure.forbidden();
        }
    }

    private static void requireMerchant(String path, AuthenticatedActor actor, AccessContext context, boolean read) {
        String[] segments = path.split("/");
        if (segments.length < 5) {
            throw SellerOidcFailure.forbidden();
        }
        boolean merchant = "merchant.account".equals(context.scopeKey()) && context.scopeId().equals(segments[4]);
        boolean storefrontRoute = segments.length >= 6 && "storefronts".equals(segments[5]);
        boolean storefront = storefrontRoute && segments.length >= 7 && "merchant.storefront".equals(context.scopeKey())
                && context.scopeId().equals(segments[6]);
        if (!merchant && !storefront) {
            throw SellerOidcFailure.forbidden();
        }
        if (storefrontRoute) {
            requireAuthority(actor, read ? "MERCHANT_STOREFRONT_READ" : "MERCHANT_STOREFRONT_WRITE");
        } else if (segments.length >= 6 && "members".equals(segments[5])) {
            if (!actor.roles().contains("MERCHANT_ADMIN")) {
                requireAuthority(actor, read ? "ACCESS_ASSIGNMENT_READ" : "ACCESS_ASSIGNMENT_WRITE");
            }
        } else {
            requireAuthority(actor, read ? "MERCHANT_PROFILE_READ" : "MERCHANT_PROFILE_WRITE");
        }
    }

    private static void requireMerchantContext(AccessContext context) {
        if (!"merchant.account".equals(context.scopeKey())) {
            throw SellerOidcFailure.forbidden();
        }
    }

    private static void requireAuthority(AuthenticatedActor actor, String authority) {
        if (!actor.authorities().contains(authority)) {
            throw SellerOidcFailure.forbidden();
        }
    }

    private static boolean under(String path, String root) {
        return path.equals(root) || path.startsWith(root + "/");
    }
}
