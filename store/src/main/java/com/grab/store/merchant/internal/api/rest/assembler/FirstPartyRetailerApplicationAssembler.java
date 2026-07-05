package com.grab.store.merchant.internal.api.rest.assembler;

import com.grab.store.merchant.internal.api.rest.controller.FirstPartyRetailerApplicationController;
import com.grab.store.merchant.internal.api.rest.controller.MerchantController;
import com.grab.store.merchant.internal.api.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@MerchantEnabled
public class FirstPartyRetailerApplicationAssembler implements RepresentationModelAssembler<GetFirstPartyRetailerApplicationResponse, EntityModel<GetFirstPartyRetailerApplicationResponse>> {

    @Override
    public EntityModel<GetFirstPartyRetailerApplicationResponse> toModel(GetFirstPartyRetailerApplicationResponse entity) {
        var model = EntityModel.of(entity,
                linkTo(methodOn(FirstPartyRetailerApplicationController.class)
                        .getFirstPartyApplication(null))
                        .withSelfRel());

        if(entity.completedBasicInfo()) {
            model.add(linkTo(methodOn(MerchantController.class)
                    .update(entity.merchantId(), null, null))
                    .withRel("edit-merchant-application"));
        }
        if(entity.completedBasicInfo() && entity.completedContactInfo() && entity.completedBusinessRegistration()) {
            model.add(linkTo(methodOn(MerchantController.class)
                    .submit(entity.merchantId(), null))
                    .withRel("submit-merchant-application"));
        }

        return model;
    }
}
