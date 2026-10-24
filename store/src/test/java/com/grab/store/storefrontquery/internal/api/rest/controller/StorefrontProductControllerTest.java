package com.grab.store.storefrontquery.internal.api.rest.controller;

import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import com.grab.store.storefrontquery.internal.api.rest.assembler.BuyableOfferModelAssembler;
import com.grab.store.storefrontquery.internal.api.rest.assembler.ShoppingContextModelAssembler;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.ShoppingContextResponse;
import com.grab.store.storefrontquery.internal.api.rest.service.StorefrontProductQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StorefrontProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({WebMvcSecurityTestConfiguration.class, BuyableOfferModelAssembler.class, ShoppingContextModelAssembler.class})
class StorefrontProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorefrontProductQueryService storefrontQueryService;

    @Test
    void get_whenProductExists_returnsEntityModelWithLinks() throws Exception {
        BuyableOfferResponse response = createResponse();
        when(storefrontQueryService.getBySlug("pk_test", "SG", "en-SG", "iced-coffee")).thenReturn(response);

        mockMvc.perform(get("/api/v1/storefront-query/products/iced-coffee")
                        .header("X-Publishable-Key", "pk_test")
                        .param("countryCode", "SG")
                        .param("locale", "en-SG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salesChannelId").value("web-1"))
                .andExpect(jsonPath("$.buyable").value(true))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.list-storefront-products.href").exists());
    }

    @Test
    void list_whenProductsExist_returnsPagedModelWithLinks() throws Exception {
        BuyableOfferResponse response = createResponse();
        when(storefrontQueryService.list(eq("pk_test"), eq("SG"), eq("en-SG"), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/storefront-query/products")
                        .header("X-Publishable-Key", "pk_test")
                        .param("countryCode", "SG")
                        .param("locale", "en-SG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.buyableOfferResponseList[0].salesChannelId").value("web-1"))
                .andExpect(jsonPath("$._embedded.buyableOfferResponseList[0].buyable").value(true))
                .andExpect(jsonPath("$._links.list-storefront-products.href").exists());
    }

    @Test
    void resolveContext_validHeader_returnsHalContext() throws Exception {
        ShoppingContextResponse response = new ShoppingContextResponse(
                "web-1", "region-1", "SG", "SGD", "en-SG", true);
        when(storefrontQueryService.resolveShoppingContext("pk_test", "SG", "en-SG")).thenReturn(response);

        mockMvc.perform(get("/api/v1/storefront-query/products/context")
                        .header("X-Publishable-Key", "pk_test")
                        .param("countryCode", "SG")
                        .param("locale", "en-SG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salesChannelId").value("web-1"))
                .andExpect(jsonPath("$.regionId").value("region-1"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    private BuyableOfferResponse createResponse() {
        return new BuyableOfferResponse(
                "web-1", "prod-1", "seller-1", "category-1", "Iced Coffee", "iced-coffee", null, null,
                "ACTIVE", true, true, true, List.of(), List.of(), "region-1", "SG", "SGD", "en-SG", true,
                true, null, null);
    }
}
