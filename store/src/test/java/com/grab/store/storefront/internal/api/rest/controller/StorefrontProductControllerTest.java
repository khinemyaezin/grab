package com.grab.store.storefront.internal.api.rest.controller;

import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import com.grab.store.storefront.internal.api.rest.assembler.StorefrontProductCardModelAssembler;
import com.grab.store.storefront.internal.api.rest.assembler.StorefrontProductDetailModelAssembler;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductCard;
import com.grab.store.storefront.internal.api.rest.service.StorefrontBrowseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StorefrontProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({WebMvcSecurityTestConfiguration.class, StorefrontProductCardModelAssembler.class, StorefrontProductDetailModelAssembler.class})
class StorefrontProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorefrontBrowseService browseService;

    @Test
    void searchReturnsPagedCardsWithPriceRangeAndStock() throws Exception {
        when(browseService.search(any(), any())).thenReturn(new PageImpl<>(List.of(
                new StorefrontProductCard(
                        "prod-1",
                        "Shirt",
                        "shirt",
                        "Apparel",
                        "cat-1",
                        "NEW",
                        true,
                        "http://cdn/hero.jpg",
                        new StorefrontProductCard.PriceRange(new BigDecimal("45900"), new BigDecimal("45900"), "mmk"),
                        true,
                        "merchant-1"
                )
        ), PageRequest.of(0, 20), 1));

        mockMvc.perform(post("/api/v1/storefront/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"shirt\",\"categoryId\":\"cat-1\",\"condition\":\"NEW\",\"featured\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.storefrontProductCardList[0].productId").value("prod-1"))
                .andExpect(jsonPath("$._embedded.storefrontProductCardList[0].priceRange.currencyCode").value("mmk"))
                .andExpect(jsonPath("$._embedded.storefrontProductCardList[0].inStock").value(true));
    }
}
