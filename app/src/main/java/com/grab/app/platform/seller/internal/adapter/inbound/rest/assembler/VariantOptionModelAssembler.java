package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerVariantOptionController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantOptionResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VariantOptionModelAssembler implements RepresentationModelAssembler<VariantOptionResponse, EntityModel<VariantOptionResponse>> {

    @Override
    public EntityModel<VariantOptionResponse> toModel(VariantOptionResponse option) {
        if (option == null) {
            return null;
        }
        EntityModel<VariantOptionResponse> model = EntityModel.of(option);
        if (option.typeId() != null) {
            model.add(linkTo(methodOn(SellerVariantOptionController.class).listVariantOptions(null, option.typeId())).withSelfRel());
        }
        return model;
    }
}
