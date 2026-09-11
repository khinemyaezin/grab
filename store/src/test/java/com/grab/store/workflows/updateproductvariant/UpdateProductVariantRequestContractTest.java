package com.grab.store.workflows.updateproductvariant;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.workflows.internal.updateproductvariant.rest.dto.request.UpdateProductVariantRequest;
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
                  "adjustStock": {
                    "inventoryItemId": "inv-1",
                    "newOnHandQuantity": 10,
                    "reason": "CORRECTION"
                  }
                }
                """, UpdateProductVariantRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.price().amount()).isEqualByComparingTo("19.99");
        assertThat(request.adjustStock().reason()).isEqualTo(AdjustmentReason.CORRECTION);
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
        assertThat(request.adjustStock()).isNull();
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
    void validate_adjustStockWithoutInventoryItemId_shouldFail() throws Exception {
        UpdateProductVariantRequest request = json.readValue("""
                {
                  "productId": "prod-1",
                  "variantId": "variant-1",
                  "sku": "SKU-1",
                  "adjustStock": {
                    "newOnHandQuantity": 8,
                    "reason": "CORRECTION"
                  }
                }
                """, UpdateProductVariantRequest.class);

        assertThat(propertyPaths(validator.validate(request))).contains("adjustStock.inventoryItemId");
    }

    private Set<String> propertyPaths(Set<ConstraintViolation<UpdateProductVariantRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
