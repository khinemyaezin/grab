package com.grab.store.merchant.internal.api.rest.dto.request;

import com.merchant.domain.enums.MerchantType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_withCallerSuppliedActorId_shouldRejectRequest() {
        StartMerchantApplicationRequest request = new StartMerchantApplicationRequest(
                MerchantType.RETAILER, "Acme Store", "spoofed-user");

        var violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("actorId"));
    }
}
