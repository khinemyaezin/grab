package com.grab.store.storefrontquery.internal.api.rest.controller;

import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import com.grab.store.storefrontquery.internal.api.rest.assembler.BuyableOfferModelAssembler;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StorefrontProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({WebMvcSecurityTestConfiguration.class, BuyableOfferModelAssembler.class})
class StorefrontProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorefrontProductQueryService storefrontQueryService;

    @Test
    void get_whenProductExists_returnsEntityModelWithLinks() throws Exception {
        BuyableOfferResponse response = createResponse();

        when(storefrontQueryService.getBySlug("web-1", "iced-coffee")).thenReturn(response);

        mockMvc.perform(get("/api/v1/storefront-query/products/iced-coffee?salesChannelId=web-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salesChannelId").value("web-1"))
                .andExpect(jsonPath("$.productId").value("prod-1"))
                .andExpect(jsonPath("$.title").value("Iced Coffee"))
                .andExpect(jsonPath("$.slug").value("iced-coffee"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.list-storefront-products.href").exists());
    }

    @Test
    void list_whenProductsExist_returnsPagedModelWithLinks() throws Exception {
        BuyableOfferResponse response = createResponse();

        when(storefrontQueryService.list(eq("web-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/storefront-query/products?salesChannelId=web-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.buyableOfferResponseList[0].salesChannelId").value("web-1"))
                .andExpect(jsonPath("$._embedded.buyableOfferResponseList[0].slug").value("iced-coffee"))
                .andExpect(jsonPath("$._links.list-storefront-products.href").exists());
    }

    private BuyableOfferResponse createResponse() {
        return new BuyableOfferResponse(
                "web-1",
                "prod-1",
                "seller-1",
                "Iced Coffee",
                "iced-coffee",
                null,
                null,
                "ACTIVE",
                true,
                true,
                "MMK",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(5000),
                List.of(),
                List.of()
        );
    }
}
