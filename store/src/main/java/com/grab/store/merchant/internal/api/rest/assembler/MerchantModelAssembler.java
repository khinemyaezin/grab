package com.grab.store.merchant.internal.api.rest.assembler;

import com.grab.store.merchant.internal.api.rest.controller.MerchantController;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.merchant.domain.enums.MerchantStatus;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@MerchantEnabled
public class MerchantModelAssembler implements RepresentationModelAssembler<MerchantResponse, EntityModel<MerchantResponse>> {

    @Override
    public EntityModel<MerchantResponse> toModel(MerchantResponse response) {
        EntityModel<MerchantResponse> model = EntityModel.of(
                response,
                linkTo(methodOn(MerchantController.class)
                        .getMerchant(response.merchantId(), null))
                        .withSelfRel()
        );

        if (isEditableApplication(response.status())) {
            model.add(linkTo(methodOn(MerchantController.class)
                    .update(response.merchantId(), null, null))
                    .withRel("edit-merchant-application"));
            model.add(linkTo(methodOn(MerchantController.class)
                    .submit(response.merchantId(), null))
                    .withRel("submit-merchant-application"));
        }
        return model;
    }

    private boolean isEditableApplication(String status) {
        return MerchantStatus.DRAFT.name().equals(status) || MerchantStatus.CHANGES_REQUESTED.name().equals(status);
    }
}
