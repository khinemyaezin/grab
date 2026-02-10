package com.grab.store.catalog.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductCombinationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(ProductCombinationTestConfig.class)
class ProductVariationCombinationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ENDPOINT = "/api/v1/products/combination";
    private static final String PRODUCT_ID = "prod-lifecycle-1";
    private static final String CATEGORY_ID = "cat-1";

    /**
     *  Adding New Variant Type, should delete OLD record.
     *  BEFORE: Red-Large
     *  AFTER:  Red-Large (DELETED)
     *          Red-Large-Men (NEW)
     *          Red-Large-Women (NEW)
     */
    @Test
    void addingNewVariantType_shouldDeleteOldRecord() throws Exception {
        List<ProductCombinationRequest.Variant> existingVariants = List.of(
                variant("variant-1", "SKU-RED-LARGE", "ACTIVE",
                        variation("color-red", "Red", "color", "Color"),
                        variation("size-large", "Large", "size", "Size")
                )
        );

        ProductCombinationRequest request = new ProductCombinationRequest(
                new ProductCombinationRequest.Product(
                        PRODUCT_ID,
                        "T-Shirt",
                        CATEGORY_ID,
                        existingVariants
                ),
                List.of(
                        variantType("color", "Color",
                                variantOption("color-red", "Red")
                        ),
                        variantType("size", "Size",
                                variantOption("size-large", "Large")
                        ),
                        variantType("gender", "Gender",  // NEW variant type
                                variantOption("gender-male", "Male"),
                                variantOption("gender-female", "Female")
                        )
                )
        );

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ProductCombinationResponse response = extractResponse(result);

        assertThat(response.product().variants()).hasSize(2);

        List<String> resultSkus = response.product().variants().stream()
                .map(ProductCombinationResponse.Variant::sku)
                .toList();
        assertThat(resultSkus).doesNotContain("SKU-RED-LARGE"); // Old variant not in result

        assertThat(response.product().variants())
                .allMatch(v -> v.variations().size() == 3)
                .allMatch(v -> v.status().equals("ACTIVE"));

       List<String> gendersInOrder = response.product().variants().stream()
               .map(v -> v.variations().stream()
                       .filter(var -> var.typeName().equals("Gender"))
                       .map(ProductCombinationResponse.Variation::optionName)
                       .findFirst()
                       .orElse(null))
               .toList();
       assertThat(gendersInOrder).containsExactly("Male", "Female");
    }

    /**
     * Removing Variant Type, should delete old record
     * BEFORE: Red-Large-Men
     *         Red-Large-Women
     * AFTER:  Red-Large-Men (DELETED)
     *         Red-Large-Women (DELETED)
     *         Red-Large (NEW)
     */
    @Test
    void removingVariantType_createsNewRecordAndDeleteOldRecords() throws Exception {
        List<ProductCombinationRequest.Variant> existingVariants = List.of(
                variant("variant-1", "SKU-RED-LARGE-MEN", "ACTIVE",
                        variation("color-red", "Red", "color", "Color"),
                        variation("size-large", "Large", "size", "Size"),
                        variation("gender-male", "Male", "gender", "Gender")
                ),
                variant("variant-2", "SKU-RED-LARGE-WOMEN", "ACTIVE",
                        variation("color-red", "Red", "color", "Color"),
                        variation("size-large", "Large", "size", "Size"),
                        variation("gender-female", "Female", "gender", "Gender")
                )
        );

        // AFTER: Remove Gender variant type
        ProductCombinationRequest request = new ProductCombinationRequest(
                new ProductCombinationRequest.Product(
                        PRODUCT_ID,
                        "T-Shirt",
                        CATEGORY_ID,
                        existingVariants
                ),
                List.of(
                        variantType("color", "Color",
                                variantOption("color-red", "Red")
                        ),
                        variantType("size", "Size",
                                variantOption("size-large", "Large")
                        )
                )
        );

        // When
        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ProductCombinationResponse response = extractResponse(result);

        assertThat(response.product().variants()).hasSize(1);

        List<String> resultSkus = response.product().variants().stream()
                .map(ProductCombinationResponse.Variant::sku)
                .toList();
        assertThat(resultSkus).doesNotContain("SKU-RED-LARGE-MEN", "SKU-RED-LARGE-WOMEN");

        ProductCombinationResponse.Variant newVariant = response.product().variants().get(0);
        assertThat(newVariant.variations()).hasSize(2);
        assertThat(newVariant.variations())
                .extracting(ProductCombinationResponse.Variation::typeName)
                .containsExactlyInAnyOrder("Color", "Size");
        assertThat(newVariant.variations())
                .extracting(ProductCombinationResponse.Variation::typeName)
                .doesNotContain("Gender");
    }

    /**
     * Adding Option, should keep existing records.
     * BEFORE: Red-Large
     *         Yellow-Large
     * AFTER:  Red-Large
     *         Yellow-Large
     *         Blue-Large (NEW)
     */
    @Test
    void addingOptionToExistingType_keepsExistingAndAddsNew() throws Exception {
        List<ProductCombinationRequest.Variant> existingVariants = List.of(
                variant("variant-red", "SKU-RED-LARGE", "ACTIVE",
                        variation("color-red", "Red", "color", "Color"),
                        variation("size-large", "Large", "size", "Size")
                ),
                variant("variant-yellow", "SKU-YELLOW-LARGE", "ACTIVE",
                        variation("color-yellow", "Yellow", "color", "Color"),
                        variation("size-large", "Large", "size", "Size")
                )
        );

        ProductCombinationRequest request = new ProductCombinationRequest(
                new ProductCombinationRequest.Product(
                        PRODUCT_ID,
                        "T-Shirt",
                        CATEGORY_ID,
                        existingVariants
                ),
                List.of(
                        variantType("color", "Color",
                                variantOption("color-red", "Red"),
                                variantOption("color-yellow", "Yellow"),
                                variantOption("color-blue", "Blue")  // NEW option
                        ),
                        variantType("size", "Size",
                                variantOption("size-large", "Large")
                        )
                )
        );

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ProductCombinationResponse response = extractResponse(result);

        assertThat(response.product().variants()).hasSize(3);

        List<String> resultSkus = response.product().variants().stream()
                .map(ProductCombinationResponse.Variant::sku)
                .toList();
        assertThat(resultSkus).contains("SKU-RED-LARGE", "SKU-YELLOW-LARGE");

        assertThat(response.product().variants())
                .anyMatch(v -> v.variations().stream()
                        .anyMatch(var -> var.optionName().equals("Blue")));

        assertThat(response.product().variants())
                .allMatch(v -> v.variations().size() == 2)
                .allMatch(v -> v.status().equals("ACTIVE"));
    }

    /**
     * Removing Option, should delete affected record.
     * BEFORE: Red-Large
     *         Yellow-Large
     *         Blue-Large
     * AFTER:  Red-Large    (KEEP)
     *         Yellow-Large (KEEP)
     *         Blue-Large   (DELETED)
     */
    @Test
    void removingOptionFromType_deletesAffectedRecord() throws Exception {
        List<ProductCombinationRequest.Variant> existingVariants = List.of(
                variant("variant-red", "SKU-RED-LARGE", "ACTIVE",
                        variation("color-red", "Red", "color", "Color"),
                        variation("size-large", "Large", "size", "Size")
                ),
                variant("variant-yellow", "SKU-YELLOW-LARGE", "ACTIVE",
                        variation("color-yellow", "Yellow", "color", "Color"),
                        variation("size-large", "Large", "size", "Size")
                ),
                variant("variant-blue", "SKU-BLUE-LARGE", "ACTIVE",
                        variation("color-blue", "Blue", "color", "Color"),
                        variation("size-large", "Large", "size", "Size")
                )
        );

        ProductCombinationRequest request = new ProductCombinationRequest(
                new ProductCombinationRequest.Product(
                        PRODUCT_ID,
                        "T-Shirt",
                        CATEGORY_ID,
                        existingVariants
                ),
                List.of(
                        variantType("color", "Color",
                                variantOption("color-red", "Red"),
                                variantOption("color-yellow", "Yellow")
                                // Blue option removed.
                        ),
                        variantType("size", "Size",
                                variantOption("size-large", "Large")
                        )
                )
        );

        MvcResult result = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ProductCombinationResponse response = extractResponse(result);

        assertThat(response.product().variants()).hasSize(2);

        List<String> resultSkus = response.product().variants().stream()
                .map(ProductCombinationResponse.Variant::sku)
                .toList();
        assertThat(resultSkus).containsExactlyInAnyOrder("SKU-RED-LARGE", "SKU-YELLOW-LARGE");

        assertThat(resultSkus).doesNotContain("SKU-BLUE-LARGE");

        assertThat(response.product().variants())
                .allMatch(v -> v.status().equals("ACTIVE"));

        assertThat(response.product().variants())
                .noneMatch(v -> v.variations().stream()
                        .anyMatch(var -> var.optionName().equals("Blue")));
    }

    private ProductCombinationResponse extractResponse(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readValue(json, ProductCombinationResponse.class);
    }

    private ProductCombinationRequest.VariantType variantType(String typeId, String typeName,
                                                               ProductCombinationRequest.VariantOption... options) {
        return new ProductCombinationRequest.VariantType(typeId, typeName, List.of(options));
    }

    private ProductCombinationRequest.VariantOption variantOption(String optionId, String optionName) {
        return new ProductCombinationRequest.VariantOption(optionId, optionName);
    }

    private ProductCombinationRequest.Variant variant(String id, String sku, String status,
                                                       ProductCombinationRequest.Variation... variations) {
        return new ProductCombinationRequest.Variant(id, sku, status, List.of(variations));
    }

    private ProductCombinationRequest.Variation variation(String optionId, String optionName,
                                                           String typeId, String typeName) {
        return new ProductCombinationRequest.Variation(optionName, optionId, typeId, typeName);
    }
}
