package com.grab.store.workflows.createsellableproduct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.workflows.internal.workflows.createsellableproduct.rest.dto.request.CreateSellableProductRequest;
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

    @Test
    void optionalStatusAndPublicationLines_shouldDeserialize() throws Exception {
        CreateSellableProductRequest request = json.readValue("""
                {
                  "product": {
                    "name": "Shirt",
                    "categoryId": "cat-1",
                    "status": "ACTIVE",
                    "variants": [{ "sku": "SKU-1", "variations": [], "manageInventory": false }]
                  },
                  "pricingLines": [
                    { "sku": "SKU-1", "currencyCode": "USD", "amount": 19.99 }
                  ],
                  "publicationLines": [
                    { "sku": "SKU-1", "salesChannelId": "web-1" }
                  ]
                }
                """, CreateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.product().status()).isEqualTo("ACTIVE");
        assertThat(request.publicationLines()).containsExactly(
                new CreateSellableProductRequest.PublicationLine("SKU-1", "web-1")
        );
    }

    @Test
    void omittedStatusAndPublicationLines_shouldBeNull() throws Exception {
        CreateSellableProductRequest request = json.readValue("""
                {
                  "product": {
                    "name": "Shirt",
                    "categoryId": "cat-1",
                    "variants": [{ "sku": "SKU-1", "variations": [], "manageInventory": false }]
                  },
                  "pricingLines": [
                    { "sku": "SKU-1", "currencyCode": "USD", "amount": 19.99 }
                  ]
                }
                """, CreateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.product().status()).isNull();
        assertThat(request.publicationLines()).isNull();
    }

    @Test
    void variantWithoutPricingLine_shouldFail() throws Exception {
        CreateSellableProductRequest request = json.readValue("""
                {
                  "product": {
                    "name": "Shirt",
                    "categoryId": "cat-1",
                    "variants": [{ "sku": "SKU-1", "variations": [], "manageInventory": false }]
                  },
                  "pricingLines": [
                    { "sku": "SKU-OTHER", "currencyCode": "USD", "amount": 19.99 }
                  ]
                }
                """, CreateSellableProductRequest.class);

        assertThat(propertyPaths(validator.validate(request))).isNotEmpty();
    }

    @Test
    void omittedListing_shouldBeNull() throws Exception {
        CreateSellableProductRequest request = json.readValue("""
                {
                  "product": {
                    "name": "Shirt",
                    "categoryId": "cat-1",
                    "variants": [{ "sku": "SKU-1", "variations": [], "manageInventory": false }]
                  },
                  "pricingLines": [
                    { "sku": "SKU-1", "currencyCode": "USD", "amount": 19.99 }
                  ]
                }
                """, CreateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.medias()).isNull();
        assertThat(request.descriptions()).isNull();
    }

    @Test
    void presentListing_shouldDeserializeMediasAndDescriptions() throws Exception {
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
                  "medias": [
                    { "storageKey": "staged/hero.jpg", "contentType": "image/jpeg", "rank": 0 }
                  ],
                  "descriptions": [
                    { "name": "overview", "title": "Overview", "description": "A cotton shirt" }
                  ]
                }
                """, CreateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.medias()).containsExactly(
                new CreateSellableProductRequest.Media(null, "staged/hero.jpg", "image/jpeg", 0)
        );
        assertThat(request.descriptions()).containsExactly(
                new CreateSellableProductRequest.Description(null, "overview", "Overview", "A cotton shirt")
        );
    }

    @Test
    void emptyListing_shouldDeserializeAsEmptyLists() throws Exception {
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
                  "medias": [],
                  "descriptions": []
                }
                """, CreateSellableProductRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.medias()).isEmpty();
        assertThat(request.descriptions()).isEmpty();
    }

    private Set<String> propertyPaths(Set<ConstraintViolation<CreateSellableProductRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
