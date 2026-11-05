package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPriceSetController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PriceSetModelAssembler implements RepresentationModelAssembler<PriceSetResponse, EntityModel<PriceSetResponse>> {

    @Override
    public EntityModel<PriceSetResponse> toModel(PriceSetResponse priceSet) {
        if (priceSet == null) {
            return null;
        }
        EntityModel<PriceSetResponse> model = EntityModel.of(priceSet);
        if (priceSet.id() != null) {
            model.add(linkTo(methodOn(SellerPriceSetController.class).getPriceSet(priceSet.id())).withSelfRel());
        }
        return model;
    }
}
