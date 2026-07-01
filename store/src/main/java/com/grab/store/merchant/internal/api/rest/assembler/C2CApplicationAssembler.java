package com.grab.store.merchant.internal.api.rest.assembler;

import com.grab.store.merchant.internal.api.rest.controller.MerchantController;
import com.grab.store.merchant.internal.api.rest.dto.response.GetC2CApplicationResponse;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@MerchantEnabled
public class C2CApplicationAssembler implements RepresentationModelAssembler<GetC2CApplicationResponse, EntityModel<GetC2CApplicationResponse>> {

    @Override
    public EntityModel<GetC2CApplicationResponse> toModel(GetC2CApplicationResponse entity) {
        var model = EntityModel.of(entity,
                linkTo(methodOn(MerchantController.class)
                        .getC2CApplication(null))
                        .withSelfRel());
        if(entity.completedBasicInfo()) {
            model.add(linkTo(methodOn(MerchantController.class)
                    .update(entity.merchantId(),null, null))
                    .withRel("edit-merchant-application"));
        }
        if(entity.completedBasicInfo() && entity.completedContactInfo()) {
            model.add(linkTo(methodOn(MerchantController.class)
                    .submit(entity.merchantId(),null))
                    .withRel("submit-merchant-application"));
        }

        return model;
    }
}
