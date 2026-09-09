package com.grab.store.workflows.createsellableproduct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.workflows.internal.createsellableproduct.rest.dto.request.CreateSellableProductRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductRequestContractTest {

    private final ObjectMapper json = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void trackedVariantWithoutInventoryLines_shouldFail() throws Exception {
        CreateSellableProductRequest request = json.readValue("""
                {
                  "product": {
                    "name": "Shirt",
                    "categoryId": "cat-1",
                    "variants": [{ "sku": "SKU-1", "variations": [], "manageInventory": true }]
                  },
                  "pricingLines": [
                    { "sku": "SKU-1", "currencyCode": "USD", "amount": 19.99 }
                  ],
                  "inventoryLines": []
                }
                """, CreateSellableProductRequest.class);

        assertThat(propertyPaths(validator.validate(request))).isNotEmpty();
    }

    @Test
    void untrackedVariantWithInventoryLines_shouldFail() throws Exception {
        CreateSellableProductRequest request = json.readValue("""
                {
                  "product": {
                    "name": "Shirt",
                    "categoryId": "cat-1",
                    "variants": [{ "sku": "SKU-1", "variations": [], "manageInventory": false }]
                  },
                  "pricingLines": [
                    { "sku": "SKU-1", "currencyCode": "USD", "amount": 19.99 }
                  ],
                  "inventoryLines": [
                    { "sku": "SKU-1", "locationId": "loc-1", "initialQuantity": 1 }
                  ]
                }
                """, CreateSellableProductRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines");
    }

    @Test
    void untrackedVariantWithoutInventoryLines_shouldBeValid() throws Exception {
        CreateSellableProductRequest request = json.readValue("""
                {
                  "product": {
                    "name": "Shirt",
                    "categoryId": "cat-1",
                    "variants": [{ "sku": "SKU-1", "variations": [], "manageInventory": false }]
                  },
                  "pricingLines": [
                    { "sku": "SKU-1", "currencyCode": "USD", "amount": 19.99 }
                  ],
                  "inventoryLines": []
                }
                """, CreateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.product().variants().getFirst().manageInventory()).isFalse();
    }

    @Test
    void omittedManageInventory_shouldBeUntrackedAndRejectInventoryLines() throws Exception {
        CreateSellableProductRequest request = json.readValue("""
                {
                  "product": {
                    "name": "Shirt",
                    "categoryId": "cat-1",
                    "variants": [{ "sku": "SKU-1", "variations": [] }]
                  },
                  "pricingLines": [
                    { "sku": "SKU-1", "currencyCode": "USD", "amount": 19.99 }
                  ],
                  "inventoryLines": [
                    { "sku": "SKU-1", "locationId": "loc-1", "initialQuantity": 4 }
                  ]
                }
                """, CreateSellableProductRequest.class);

        assertThat(request.product().variants().getFirst().manageInventory()).isNull();
        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines");
    }

    private Set<String> propertyPaths(Set<ConstraintViolation<CreateSellableProductRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
