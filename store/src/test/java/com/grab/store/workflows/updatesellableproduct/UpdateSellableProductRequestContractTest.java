package com.grab.store.workflows.updatesellableproduct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.internal.updatesellableproduct.rest.dto.request.UpdateSellableProductRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductRequestContractTest {

    private final ObjectMapper json = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void deserialize_createLine_shouldBeValid() throws Exception {
        UpdateSellableProductRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "product": { "name": "Shirt", "categoryId": "cat-1" },
                  "inventoryLines": [
                    {
                      "sku": "SKU-NEW",
                      "locationId": "loc-1",
                      "op": "CREATE",
                      "create": { "initialQuantity": 10, "safetyStock": 2 }
                    }
                  ]
                }
                """, UpdateSellableProductRequest.class);

        assertThat(request.inventoryLines().getFirst().op()).isEqualTo(InventorySyncOp.CREATE);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deserialize_adjustLine_shouldBeValid() throws Exception {
        UpdateSellableProductRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "product": { "name": "Shirt", "categoryId": "cat-1" },
                  "inventoryLines": [
                    {
                      "sku": "SKU-A",
                      "inventoryItemId": "inv-1",
                      "op": "ADJUST",
                      "adjust": { "newOnHandQuantity": 8, "reason": "CYCLE_COUNT" },
                      "reorder": { "safetyStock": 2, "reorderPoint": 5, "reorderQuantity": 20, "maxStock": 100 }
                    }
                  ]
                }
                """, UpdateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.inventoryLines().getFirst().adjust().newOnHandQuantity()).isEqualTo(8);
    }

    @Test
    void deserialize_damageAndWriteOffLines_shouldBeValid() throws Exception {
        UpdateSellableProductRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "product": { "name": "Shirt", "categoryId": "cat-1" },
                  "inventoryLines": [
                    {
                      "sku": "SKU-B",
                      "inventoryItemId": "inv-2",
                      "op": "DAMAGE",
                      "damage": { "quantity": 2, "notes": "water damage" }
                    },
                    {
                      "sku": "SKU-C",
                      "inventoryItemId": "inv-3",
                      "op": "WRITE_OFF",
                      "writeOff": { "quantity": 1, "reason": "lost" }
                    }
                  ]
                }
                """, UpdateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void validate_createWithInventoryItemId_shouldFail() throws Exception {
        UpdateSellableProductRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "product": { "name": "Shirt", "categoryId": "cat-1" },
                  "inventoryLines": [
                    {
                      "sku": "SKU-NEW",
                      "locationId": "loc-1",
                      "inventoryItemId": "inv-1",
                      "op": "CREATE",
                      "create": { "initialQuantity": 10 }
                    }
                  ]
                }
                """, UpdateSellableProductRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines[0].inventoryItemId");
    }

    @Test
    void validate_adjustWithoutNestedPayload_shouldFail() throws Exception {
        UpdateSellableProductRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "product": { "name": "Shirt", "categoryId": "cat-1" },
                  "inventoryLines": [
                    {
                      "sku": "SKU-A",
                      "inventoryItemId": "inv-1",
                      "op": "ADJUST"
                    }
                  ]
                }
                """, UpdateSellableProductRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines[0].adjust");
    }

    @Test
    void validate_duplicateInventoryItemId_shouldFail() throws Exception {
        UpdateSellableProductRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "product": { "name": "Shirt", "categoryId": "cat-1" },
                  "inventoryLines": [
                    {
                      "sku": "SKU-A",
                      "inventoryItemId": "inv-1",
                      "op": "ADJUST",
                      "adjust": { "newOnHandQuantity": 8, "reason": "CYCLE_COUNT" }
                    },
                    {
                      "sku": "SKU-A",
                      "inventoryItemId": "inv-1",
                      "op": "DAMAGE",
                      "damage": { "quantity": 1 }
                    }
                  ]
                }
                """, UpdateSellableProductRequest.class);

        assertThat(propertyPaths(validator.validate(request)))
                .anyMatch(path -> path.contains("inventoryItemId"));
    }

    @Test
    void deserialize_pricingLine_shouldAcceptSkuAndOptionalVariantId() throws Exception {
        UpdateSellableProductRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "product": { "name": "Shirt", "categoryId": "cat-1" },
                  "pricingLines": [
                    {
                      "sku": "SKU-NEW",
                      "currencyCode": "USD",
                      "amount": 19.99
                    },
                    {
                      "sku": "SKU-1",
                      "variantId": "variant-1",
                      "title": "Base",
                      "currencyCode": "USD",
                      "amount": 12.50
                    }
                  ]
                }
                """, UpdateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.pricingLines().getFirst().sku()).isEqualTo("SKU-NEW");
        assertThat(request.pricingLines().getFirst().variantId()).isNull();
        assertThat(request.pricingLines().get(1).variantId()).isEqualTo("variant-1");
    }

    private Set<String> propertyPaths(Set<ConstraintViolation<UpdateSellableProductRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
