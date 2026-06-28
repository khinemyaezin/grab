package com.grab.store.identity.internal.api.rest.dto.request;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrantAccessRequestContractTest {
    private final ObjectMapper json = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void deserialize_withScopeKey_shouldCreateValidRequest() throws Exception {
        GrantAccessRequest request = json.readValue("""
                {
                  "userId": "user-1",
                  "platformCode": "SELLER_PORTAL",
                  "roleCode": "MERCHANT_ADMIN",
                  "scopeKey": "merchant.account",
                  "scopeId": "merchant-1"
                }
                """, GrantAccessRequest.class);

        assertThat(request.scopeKey()).isEqualTo("merchant.account");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deserialize_withLegacyScopeType_shouldFailRequestValidation() throws Exception {
        GrantAccessRequest request = json.readValue("""
                {
                  "userId": "user-1",
                  "platformCode": "SELLER_PORTAL",
                  "roleCode": "MERCHANT_ADMIN",
                  "scopeType": "MERCHANT_ACCOUNT",
                  "scopeId": "merchant-1"
                }
                """, GrantAccessRequest.class);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("scopeKey");
    }
}
