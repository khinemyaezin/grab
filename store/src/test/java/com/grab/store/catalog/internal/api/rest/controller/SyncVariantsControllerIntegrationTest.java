package com.grab.store.catalog.internal.api.rest.controller;

import com.catalog.domain.aggregate.Product;
import com.grab.store.catalog.internal.api.rest.dto.request.SyncVariantsRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.SyncVariantsResponse;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.handler.InMemoryProductRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(ProductControllerTestConfig.class)
class SyncVariantsControllerIntegrationTest {

    private static final String PRODUCT_ID = "prod-sync-1";
    private static final String CATEGORY_ID = "cat-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryProductRepositoryTest productRepository;

    @Test
    void syncVariants_updatesProductVariants() throws Exception {
        Product product = Product.create(new CommonId(PRODUCT_ID), "T-Shirt", new CommonId(CATEGORY_ID));
        productRepository.put(product);

        SyncVariantsRequest request = new SyncVariantsRequest(
                List.of(
                        variantType("color", "Color",
                                variantOption("color-red", "Red")
                        ),
                        variantType("size", "Size",
                                variantOption("size-small", "Small")
                        )
                ),
                List.of(
                        variant("variant-red-small", "SKU-RED-SMALL",
                                variation("Red", "color-red", "Color", "color"),
                                variation("Small", "size-small", "Size", "size")
                        )
                )
        );

        MvcResult result = mockMvc.perform(put("/api/v1/products/{productId}/variants", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        SyncVariantsResponse response = extractResponse(result);

        assertThat(response.productId()).isEqualTo(PRODUCT_ID);
        assertThat(response.productName()).isEqualTo("T-Shirt");
        assertThat(response.variants()).hasSize(1);
        assertThat(response.variants().getFirst().id()).isEqualTo("variant-red-small");
        assertThat(response.variants().getFirst().sku()).isEqualTo("SKU-RED-SMALL");
    }

    private SyncVariantsResponse extractResponse(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, SyncVariantsResponse.class);
    }

    private SyncVariantsRequest.VariantType variantType(String typeId, String typeName,
                                                        SyncVariantsRequest.VariantOption... options) {
        return new SyncVariantsRequest.VariantType(typeId, typeName, List.of(options));
    }

    private SyncVariantsRequest.VariantOption variantOption(String optionId, String optionName) {
        return new SyncVariantsRequest.VariantOption(optionId, optionName);
    }

    private SyncVariantsRequest.Variant variant(String id, String sku,
                                                SyncVariantsRequest.Variation... variations) {
        return new SyncVariantsRequest.Variant(id, sku, List.of(variations));
    }

    private SyncVariantsRequest.Variation variation(String optionName, String optionId,
                                                    String typeName, String typeId) {
        return new SyncVariantsRequest.Variation(optionName, optionId, typeId, typeName);
    }
}
