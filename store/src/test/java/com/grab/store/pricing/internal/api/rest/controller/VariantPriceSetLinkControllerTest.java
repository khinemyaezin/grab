package com.grab.store.pricing.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.pricing.internal.api.rest.assembler.VariantPriceSetLinkModelAssembler;
import com.grab.store.pricing.internal.api.rest.dto.request.ListVariantPriceSetLinksRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.VariantPriceSetLinkResponse;
import com.grab.store.pricing.internal.api.rest.service.VariantPriceSetLinkQueryService;
import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VariantPriceSetLinkController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfiguration.class)
@TestPropertySource(properties = "pricing.enabled=true")
class VariantPriceSetLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VariantPriceSetLinkQueryService queryService;

    @MockBean
    private VariantPriceSetLinkModelAssembler modelAssembler;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        when(modelAssembler.toModel(any(VariantPriceSetLinkResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));
    }

    @Test
    void list_shouldReturnVariantPriceLinks() throws Exception {
        ListVariantPriceSetLinksRequest request = new ListVariantPriceSetLinksRequest(List.of("variant-1", "variant-2"));
        List<VariantPriceSetLinkResponse> responses = List.of(
                new VariantPriceSetLinkResponse("variant-1", "price-set-1", "product-1", "SKU-1", "merchant-1"),
                new VariantPriceSetLinkResponse("variant-2", "price-set-2", "product-1", "SKU-2", "merchant-1")
        );

        when(queryService.findByVariantIds(any(ListVariantPriceSetLinksRequest.class)))
                .thenReturn(responses);

        mockMvc.perform(post("/api/v1/pricing/variant-price-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.variantPriceSetLinkResponseList").isArray())
                .andExpect(jsonPath("$._embedded.variantPriceSetLinkResponseList[0].variantId").value("variant-1"))
                .andExpect(jsonPath("$._embedded.variantPriceSetLinkResponseList[0].priceSetId").value("price-set-1"))
                .andExpect(jsonPath("$._embedded.variantPriceSetLinkResponseList[1].variantId").value("variant-2"))
                .andExpect(jsonPath("$._embedded.variantPriceSetLinkResponseList[1].priceSetId").value("price-set-2"));
    }

    @Test
    void list_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        ListVariantPriceSetLinksRequest invalidRequest = new ListVariantPriceSetLinksRequest(List.of());

        mockMvc.perform(post("/api/v1/pricing/variant-price-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
