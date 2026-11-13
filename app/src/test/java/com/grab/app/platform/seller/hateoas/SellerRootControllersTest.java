package com.grab.app.platform.seller.hateoas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerApiRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.SellerSecurityConfig;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.SellerSecurityProperties;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.config.SellerRootSecurityConfigurer;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception.ProblemDetailAccessDeniedHandler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception.ProblemDetailAuthEntryPoint;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.util.SellerAuthCookieHelper;
import com.grab.framework.security.AccessTokenAuthenticator;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SellerRootControllersTest {

    private static final String API_ROOT = "/api/v1";
    private static final String REGION_ROOT = "/api/v1/regions";
    private static final Map<String, String> RESOURCE_ROOTS = Map.of(
            "get-identity-root", "/api/v1/identity",
            "get-catalog-root", "/api/v1/catalog",
            "get-inventory-root", "/api/v1/inventory",
            "get-merchant-root", "/api/v1/merchants/root",
            "get-pricing-root", "/api/v1/pricing",
            "get-sales-channel-root", "/api/v1/sales-channels/root",
            "get-workflows-root", "/api/v1/workflows/root",
            "get-region-root", REGION_ROOT
    );
    private static final Map<String, Set<String>> RESOURCE_RELS = Map.of(
            "/api/v1/identity", Set.of(
                    "self", "login", "logout",
                    "refresh", "register", "get-profile",
                    "list-access-contexts"),
            "/api/v1/catalog", Set.of(
                    "self", "list-products", "search-products", "get-product",
                    "get-variant", "create-product", "search-category-leaves",
                    "get-category-tree", "search-variant-options", "search-variant-types",
                    "create-sellable-product", "update-sellable-product", "update-product-variant"),
            "/api/v1/inventory", Set.of(
                    "self", "create-location", "get-location",
                    "create-inventory-item", "get-inventory-item", "get-inventory-movement-latest",
                    "allocate-stock", "deallocate-stock", "list-reorder-suggestions"),
            "/api/v1/merchants/root", Set.of(
                    "self", "list-merchants", "get-merchant",
                    "create-merchant-application", "get-c2c-application", "create-c2c-application",
                    "get-first-party-retailer-application", "create-first-party-retailer-application", "list-storefronts",
                    "get-storefront", "create-storefront", "list-region-countries"),
            "/api/v1/pricing", Set.of(
                    "self", "list-price-lists", "create-price-list",
                    "create-price-set", "calculate-prices", "create-price-preference",
                    "get-price-preference"),
            "/api/v1/regions", Set.of(
                    "self", "get-region", "list-region-countries"),
            "/api/v1/sales-channels/root", Set.of(
                    "self", "list-sales-channels", "get-sales-channel",
                    "create-merchant-website-channel", "resolve-publishable-key"),
            "/api/v1/workflows/root", Set.of(
                    "self", "get-create-sellable-product", "get-update-sellable-product",
                    "get-update-product-variant", "create-sellable-product", "update-sellable-product",
                    "update-product-variant")
    );

    @Test
    void discoversSellerResourcesAndPreservesTheirLinkContracts() {
        ExternalPrincipal principal = new ExternalPrincipal("issuer", "subject", "seller@example.com", Set.of());
        AuthenticatedActor actor = new AuthenticatedActor(
                "seller-id", "issuer", "subject", "seller@example.com", Set.of(), Set.of()
        );
        AccessTokenAuthenticator authenticator = token -> {
            assertThat(token).isEqualTo("seller-test-token");
            return principal;
        };
        PlatformIdentityResolver resolver = new PlatformIdentityResolver() {
            @Override
            public AuthenticatedActor resolve(ExternalPrincipal external) {
                assertThat(external).isEqualTo(principal);
                return actor;
            }

            @Override
            public String localIssuer() {
                return "issuer";
            }
        };

        new WebApplicationContextRunner()
                .withUserConfiguration(WebConfiguration.class)
                .withBean(AccessTokenAuthenticator.class, () -> authenticator)
                .withBean(PlatformIdentityResolver.class, () -> resolver)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    Filter security = context.getBean("springSecurityFilterChain", Filter.class);
                    MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);

                    JsonNode apiLinks = getLinks(mvc, mapper, get(API_ROOT), API_ROOT);
                    Set<String> apiRels = new HashSet<>(RESOURCE_ROOTS.keySet());
                    apiRels.add("self");
                    assertRels(apiLinks, apiRels, API_ROOT);

                    mvc.perform(get(REGION_ROOT).accept(MediaTypes.HAL_JSON))
                            .andExpect(status().isUnauthorized());

                    Map<String, JsonNode> roots = new HashMap<>();
                    for (Map.Entry<String, String> resource : RESOURCE_ROOTS.entrySet()) {
                        String path = resource.getValue();
                        assertHref(apiLinks, resource.getKey(), path);
                        MockHttpServletRequestBuilder request = get(path);
                        if (path.equals(REGION_ROOT)) {
                            request.header(HttpHeaders.AUTHORIZATION, "Bearer seller-test-token");
                        }
                        JsonNode links = getLinks(mvc, mapper, request, path);
                        assertRels(links, RESOURCE_RELS.get(path), path);
                        roots.put(path, links);
                    }

                    JsonNode region = roots.get(REGION_ROOT);
                    assertTemplate(region, "get-region", REGION_ROOT + "/{regionId}");
                    assertHref(region, "list-region-countries", REGION_ROOT + "/countries");

                    JsonNode merchant = roots.get("/api/v1/merchants/root");
                    assertTemplate(merchant, "get-merchant", "/api/v1/merchants/{merchantId}");
                    assertTemplate(merchant, "get-storefront", "/api/v1/merchants/{merchantId}/storefronts/{storefrontId}");
                    assertThat(merchant.get("list-region-countries")).isEqualTo(region.get("list-region-countries"));

                    JsonNode inventory = roots.get("/api/v1/inventory");
                    assertTemplate(inventory, "get-location", "/api/v1/facilities/locations/{locationId}");
                    assertTemplate(inventory, "get-inventory-item", "/api/v1/inventory/items/{itemId}");

                    JsonNode catalog = roots.get("/api/v1/catalog");
                    assertHref(catalog, "list-products", "/api/v1/products?page=0&size=20");
                    assertThat(catalog.path("search-products").path("href").asText()).contains("{&query}");
                    assertTemplate(catalog, "get-variant", "/api/v1/products/{productId}/variants/{variantId}");
                    JsonNode workflows = roots.get("/api/v1/workflows/root");
                    for (String rel : Set.of("create-sellable-product", "update-sellable-product", "update-product-variant")) {
                        String path = "/api/v1/workflows/" + rel;
                        assertHref(workflows, rel, path);
                        assertTemplate(workflows, "get-" + rel, path + "/{workflowId}");
                        assertThat(catalog.get(rel)).isEqualTo(workflows.get(rel));
                    }
                });
    }

    private static JsonNode getLinks(
            MockMvc mvc, ObjectMapper mapper, MockHttpServletRequestBuilder request, String path
    ) throws Exception {
        String body = mvc.perform(request.accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andReturn().getResponse().getContentAsString();
        JsonNode links = mapper.readTree(body).path("_links");
        assertHref(links, "self", path);
        return links;
    }

    private static void assertRels(JsonNode links, Set<String> expected, String path) {
        Set<String> rels = new HashSet<>();
        links.fieldNames().forEachRemaining(rels::add);
        assertThat(rels).as(path).containsExactlyInAnyOrderElementsOf(expected);
    }

    private static void assertHref(JsonNode links, String rel, String path) {
        JsonNode link = links.path(rel);
        assertThat(link.path("href").asText()).as(rel).isEqualTo("http://localhost" + path);
    }

    private static void assertTemplate(JsonNode links, String rel, String path) {
        assertHref(links, rel, path);
        assertThat(links.path(rel).path("templated").asBoolean()).as(rel).isTrue();
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration({
            JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
            WebMvcAutoConfiguration.class, HypermediaAutoConfiguration.class
    })
    @ComponentScan(basePackageClasses = {SellerApiRootController.class, SellerRootSecurityConfigurer.class})
    @Import({
            SellerSecurityConfig.class, SellerSecurityProperties.class, SellerAuthCookieHelper.class,
            ProblemDetailAuthEntryPoint.class, ProblemDetailAccessDeniedHandler.class
    })
    static class WebConfiguration {
    }
}
