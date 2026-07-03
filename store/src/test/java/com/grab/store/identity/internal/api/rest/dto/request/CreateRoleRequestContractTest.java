package com.grab.store.identity.internal.api.rest.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateRoleRequestContractTest {
    private final ObjectMapper json = new ObjectMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void deserialize_withPlatformAndAuthorities_shouldCreateValidRequest() throws Exception {
        CreateRoleRequest request = json.readValue("""
                {
                  "code": "PROFILE_EDITOR",
                  "name": "Profile Editor",
                  "description": "Edits merchant profile details",
                  "platformCode": "SELLER_PORTAL",
                  "authorityCodes": ["MERCHANT_PROFILE_READ", "MERCHANT_PROFILE_WRITE"]
                }
                """, CreateRoleRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.platformCode()).isEqualTo("SELLER_PORTAL");
        assertThat(request.authorityCodes()).containsExactlyInAnyOrder(
                "MERCHANT_PROFILE_READ",
                "MERCHANT_PROFILE_WRITE"
        );
    }

    @Test
    void deserialize_withoutAuthorities_shouldFailValidation() throws Exception {
        CreateRoleRequest request = json.readValue("""
                {
                  "code": "PROFILE_EDITOR",
                  "name": "Profile Editor",
                  "platformCode": "SELLER_PORTAL",
                  "authorityCodes": []
                }
                """, CreateRoleRequest.class);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("authorityCodes");
    }
}
