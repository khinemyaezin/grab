package com.grab.store.product.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.product.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.product.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.product.internal.api.rest.service.ProductFacadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ProductController.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductFacadeService productFacadeService;

    @Test
    void generateCombinations_withValidRequest_returnsOkWithCombinations() throws Exception {
        // Given
        ProductCombinationRequest request = ProductCombinationRequest.builder()
                .variantTypes(List.of(
                        ProductCombinationRequest.VariantTypeRequest.builder()
                                .typeId("type-1")
                                .typeName("Color")
                                .options(List.of(
                                        ProductCombinationRequest.VariantOptionRequest.builder()
                                                .optionId("opt-1")
                                                .optionName("Red")
                                                .build(),
                                        ProductCombinationRequest.VariantOptionRequest.builder()
                                                .optionId("opt-2")
                                                .optionName("Blue")
                                                .build()
                                ))
                                .build()
                ))
                .build();

        ProductCombinationResponse response = new ProductCombinationResponse(
                List.of(new ProductCombinationResponse.RequestedVariantType(
                        "type-1",
                        "Color",
                        List.of(
                                new ProductCombinationResponse.RequestedVariantOption("opt-1", "Red"),
                                new ProductCombinationResponse.RequestedVariantOption("opt-2", "Blue")
                        )
                )),
                List.of(
                        new ProductCombinationResponse.CombinationResponse(
                                "combo-1",
                                List.of(new ProductCombinationResponse.VariationResponse("opt-1", "Red", "type-1", "Color"))
                        ),
                        new ProductCombinationResponse.CombinationResponse(
                                "combo-2",
                                List.of(new ProductCombinationResponse.VariationResponse("opt-2", "Blue", "type-1", "Color"))
                        )
                ),
                2
        );

        when(productFacadeService.generateCombinations(any(ProductCombinationRequest.class)))
                .thenReturn(EntityModel.of(response));

        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCombinations").value(2))
                .andExpect(jsonPath("$.combinations").isArray())
                .andExpect(jsonPath("$.combinations.length()").value(2))
                .andExpect(jsonPath("$.requestedVariantTypes[0].typeName").value("Color"));

        verify(productFacadeService).generateCombinations(any(ProductCombinationRequest.class));
    }

    @Test
    void generateCombinations_withMultipleVariantTypes_returnsCartesianProduct() throws Exception {
        // Given
        ProductCombinationRequest request = ProductCombinationRequest.builder()
                .variantTypes(List.of(
                        ProductCombinationRequest.VariantTypeRequest.builder()
                                .typeId("type-color")
                                .typeName("Color")
                                .options(List.of(
                                        ProductCombinationRequest.VariantOptionRequest.builder()
                                                .optionId("red-id")
                                                .optionName("Red")
                                                .build()
                                ))
                                .build(),
                        ProductCombinationRequest.VariantTypeRequest.builder()
                                .typeId("type-size")
                                .typeName("Size")
                                .options(List.of(
                                        ProductCombinationRequest.VariantOptionRequest.builder()
                                                .optionId("small-id")
                                                .optionName("Small")
                                                .build()
                                ))
                                .build()
                ))
                .build();

        ProductCombinationResponse response = new ProductCombinationResponse(
                List.of(
                        new ProductCombinationResponse.RequestedVariantType(
                                "type-color", "Color",
                                List.of(new ProductCombinationResponse.RequestedVariantOption("red-id", "Red"))
                        ),
                        new ProductCombinationResponse.RequestedVariantType(
                                "type-size", "Size",
                                List.of(new ProductCombinationResponse.RequestedVariantOption("small-id", "Small"))
                        )
                ),
                List.of(
                        new ProductCombinationResponse.CombinationResponse(
                                "combo-1",
                                List.of(
                                        new ProductCombinationResponse.VariationResponse("red-id", "Red", "type-color", "Color"),
                                        new ProductCombinationResponse.VariationResponse("small-id", "Small", "type-size", "Size")
                                )
                        )
                ),
                1
        );

        when(productFacadeService.generateCombinations(any(ProductCombinationRequest.class)))
                .thenReturn(EntityModel.of(response));

        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCombinations").value(1))
                .andExpect(jsonPath("$.combinations[0].variations.length()").value(2))
                .andExpect(jsonPath("$.requestedVariantTypes.length()").value(2));
    }

    @Test
    void generateCombinations_withEmptyVariantTypes_returnsBadRequest() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "variantTypes": []
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateCombinations_withMissingVariantTypes_returnsBadRequest() throws Exception {
        // Given
        String invalidRequest = "{}";

        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateCombinations_withEmptyOptions_returnsBadRequest() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "variantTypes": [
                        {
                            "typeId": "type-1",
                            "typeName": "Color",
                            "options": []
                        }
                    ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateCombinations_withMissingTypeName_returnsBadRequest() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "variantTypes": [
                        {
                            "typeId": "type-1",
                            "options": [
                                {
                                    "optionId": "opt-1",
                                    "optionName": "Red"
                                }
                            ]
                        }
                    ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateCombinations_withMissingOptionName_returnsBadRequest() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "variantTypes": [
                        {
                            "typeId": "type-1",
                            "typeName": "Color",
                            "options": [
                                {
                                    "optionId": "opt-1"
                                }
                            ]
                        }
                    ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateCombinations_withInvalidContentType_returnsUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void generateCombinations_withNullTypeId_acceptsRequest() throws Exception {
        // Given - typeId is optional, so null should be accepted
        ProductCombinationRequest request = ProductCombinationRequest.builder()
                .variantTypes(List.of(
                        ProductCombinationRequest.VariantTypeRequest.builder()
                                .typeId(null)
                                .typeName("Color")
                                .options(List.of(
                                        ProductCombinationRequest.VariantOptionRequest.builder()
                                                .optionId(null)
                                                .optionName("Red")
                                                .build()
                                ))
                                .build()
                ))
                .build();

        ProductCombinationResponse response = new ProductCombinationResponse(
                List.of(new ProductCombinationResponse.RequestedVariantType(
                        null, "Color",
                        List.of(new ProductCombinationResponse.RequestedVariantOption(null, "Red"))
                )),
                List.of(new ProductCombinationResponse.CombinationResponse(
                        "combo-1",
                        List.of(new ProductCombinationResponse.VariationResponse(null, "Red", null, "Color"))
                )),
                1
        );

        when(productFacadeService.generateCombinations(any(ProductCombinationRequest.class)))
                .thenReturn(EntityModel.of(response));

        // When & Then
        mockMvc.perform(post("/api/v1/products/combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCombinations").value(1));
    }
}