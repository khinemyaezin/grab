package com.grab.store.merchant.internal.api.rest.assembler;

import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantModelAssemblerTest {
    private final MerchantModelAssembler assembler = new MerchantModelAssembler();

    @Test
    void toModel_withDraftApplication_shouldExposeLifecycleActionsUsingStandardRelations() {
        EntityModel<MerchantResponse> model = assembler.toModel(response("DRAFT"));

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("update-merchant-application")).isPresent();
        assertThat(model.getLink("submit-merchant-application")).isPresent();
        assertThat(model.getLink("edit-merchant-application")).isEmpty();
    }

    @Test
    void toModel_withActiveMerchant_shouldNotExposeApplicationActions() {
        EntityModel<MerchantResponse> model = assembler.toModel(response("ACTIVE"));

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("update-merchant-application")).isEmpty();
        assertThat(model.getLink("submit-merchant-application")).isEmpty();
    }

    private MerchantResponse response(String status) {
        return new MerchantResponse(
                "merchant-1",
                "applicant-1",
                "FIRST_PARTY_RETAILER",
                "Acme Incorporated",
                "Acme Store",
                null,
                null,
                null,
                status,
                null,
                null,
                null,
                null,
                null,
                0L
        );
    }
}
