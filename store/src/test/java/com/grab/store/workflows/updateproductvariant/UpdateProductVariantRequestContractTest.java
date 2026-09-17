package com.grab.store.workflows.updateproductvariant;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.request.UpdateProductVariantRequest;
import com.inventory.domain.enums.AdjustmentReason;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductVariantRequestContractTest {

    private final ObjectMapper json = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void deserialize_fullSnapshot_shouldBeValid() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "TSHIRT-RED-L",
                  "price": {
                    "title": "Base",
                    "currencyCode": "USD",
                    "amount": 19.99
                  },
                  "inventoryLines": [
                    {
                      "sku": "TSHIRT-RED-L",
                      "inventoryItemId": "inv-1",
                      "op": "ADJUST",
                      "adjust": { "newOnHandQuantity": 10, "reason": "CORRECTION" }
                    }
                  ]
                }
                """, UpdateProductVariantRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.price().amount()).isEqualByComparingTo("19.99");
        assertThat(request.inventoryLines().getFirst().op()).isEqualTo(InventorySyncOp.ADJUST);
        assertThat(request.inventoryLines().getFirst().adjust().reason()).isEqualTo(AdjustmentReason.CORRECTION);
    }

    @Test
    void deserialize_catalogOnly_shouldBeValid() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1"
                }
                """, UpdateProductVariantRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.price()).isNull();
        assertThat(request.inventoryLines()).isNull();
        assertThat(request.manageInventory()).isNull();
    }

    @Test
    void deserialize_manageInventoryTrue_shouldBeValid() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "manageInventory": true
                }
                """, UpdateProductVariantRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.manageInventory()).isTrue();
    }

    @Test
    void deserialize_manageInventoryFalse_shouldBeValid() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "manageInventory": false
                }
                """, UpdateProductVariantRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.manageInventory()).isFalse();
    }

    @Test
    void validate_createWhenManageInventoryFalse_shouldFail() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "manageInventory": false,
                  "inventoryLines": [
                    {
                      "sku": "SKU-1",
                      "locationId": "loc-1",
                      "op": "CREATE",
                      "create": { "initialQuantity": 10 }
                    }
                  ]
                }
                """, UpdateProductVariantRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines.sku");
    }

    @Test
    void deserialize_createLine_shouldBeValid() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "inventoryLines": [
                    {
                      "sku": "SKU-1",
                      "locationId": "loc-1",
                      "op": "CREATE",
                      "create": { "initialQuantity": 10, "safetyStock": 2 }
                    }
                  ]
                }
                """, UpdateProductVariantRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.inventoryLines().getFirst().op()).isEqualTo(InventorySyncOp.CREATE);
    }

    @Test
    void validate_missingSku_shouldFail() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1"
                }
                """, UpdateProductVariantRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("sku");
    }

    @Test
    void validate_priceWithoutAmount_shouldFail() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "price": {
                    "currencyCode": "USD"
                  }
                }
                """, UpdateProductVariantRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("price.amount");
    }

    @Test
    void validate_adjustWithoutInventoryItemId_shouldFail() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "inventoryLines": [
                    {
                      "sku": "SKU-1",
                      "op": "ADJUST",
                      "adjust": { "newOnHandQuantity": 8, "reason": "CORRECTION" }
                    }
                  ]
                }
                """, UpdateProductVariantRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines[0].inventoryItemId");
    }

    @Test
    void validate_createWithInventoryItemId_shouldFail() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "inventoryLines": [
                    {
                      "sku": "SKU-1",
                      "locationId": "loc-1",
                      "inventoryItemId": "inv-1",
                      "op": "CREATE",
                      "create": { "initialQuantity": 10 }
                    }
                  ]
                }
                """, UpdateProductVariantRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines[0].inventoryItemId");
    }

    @Test
    void validate_inventorySkuMismatch_shouldFail() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "inventoryLines": [
                    {
                      "sku": "SKU-OTHER",
                      "inventoryItemId": "inv-1",
                      "op": "ADJUST",
                      "adjust": { "newOnHandQuantity": 8, "reason": "CORRECTION" }
                    }
                  ]
                }
                """, UpdateProductVariantRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines.sku");
    }

    @Test
    void validate_duplicateInventoryItemId_shouldFail() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "inventoryLines": [
                    {
                      "sku": "SKU-1",
                      "inventoryItemId": "inv-1",
                      "op": "ADJUST",
                      "adjust": { "newOnHandQuantity": 8, "reason": "CORRECTION" }
                    },
                    {
                      "sku": "SKU-1",
                      "inventoryItemId": "inv-1",
                      "op": "DAMAGE",
                      "damage": { "quantity": 1 }
                    }
                  ]
                }
                """, UpdateProductVariantRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("inventoryLines.inventoryItemId");
    }

    private Set<String> propertyPaths(Set<ConstraintViolation<UpdateProductVariantRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
