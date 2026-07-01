package com.grab.store.merchant.internal.api.rest.assembler;

import com.grab.store.merchant.internal.api.rest.controller.MerchantController;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@MerchantEnabled
public class MerchantModelAssembler implements RepresentationModelAssembler<MerchantResponse, EntityModel<MerchantResponse>> {

    @Override
    public EntityModel<MerchantResponse> toModel(MerchantResponse entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(MerchantController.class)
                        .getMerchant(entity.merchantId(), null))
                        .withSelfRel(),
                linkTo(methodOn(MerchantController.class)
                        .listMyMerchants(null))
                        .withRel("list-merchants"),
                linkTo(methodOn(MerchantController.class)
                        .update(entity.merchantId(), null, null))
                        .withRel("edit-merchant-application"),
                linkTo(methodOn(MerchantController.class)
                        .submit(entity.merchantId(), null))
                        .withRel("submit-merchant-application")
        );
    }
}
