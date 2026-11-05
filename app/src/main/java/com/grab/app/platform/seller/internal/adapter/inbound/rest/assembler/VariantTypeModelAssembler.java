package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerVariantOptionController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantTypeResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VariantTypeModelAssembler implements RepresentationModelAssembler<VariantTypeResponse, EntityModel<VariantTypeResponse>> {

    @Override
    public EntityModel<VariantTypeResponse> toModel(VariantTypeResponse type) {
        if (type == null) {
            return null;
        }
        EntityModel<VariantTypeResponse> model = EntityModel.of(type);
        model.add(linkTo(methodOn(SellerVariantOptionController.class).listVariantOptions(null, type.id())).withRel("list-variant-options"));
        return model;
    }
}
