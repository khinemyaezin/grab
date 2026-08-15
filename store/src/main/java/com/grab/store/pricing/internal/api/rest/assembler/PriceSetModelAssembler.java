package com.grab.store.pricing.internal.api.rest.assembler;

import com.grab.store.pricing.internal.api.rest.controller.PriceSetController;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceResponse;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceSetResponse;
import com.grab.store.pricing.internal.config.PricingEnabled;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@PricingEnabled
public class PriceSetModelAssembler
        implements RepresentationModelAssembler<PriceSetResponse, EntityModel<PriceSetResponse>> {

    @Override
    public EntityModel<PriceSetResponse> toModel(PriceSetResponse response) {
        String id = response.id();
        EntityModel<PriceSetResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(PriceSetController.class).getPriceSet(id)).withSelfRel());
        model.add(linkTo(methodOn(PriceSetController.class).addPrice(id, null)).withRel("add-price-set-price"));
        model.add(linkTo(methodOn(PriceSetController.class).deletePriceSet(id)).withRel("delete-price-set"));
        model.add(linkTo(methodOn(PriceSetController.class).calculatePrices(null)).withRel("calculate-prices"));

        if (response.prices() != null) {
            model.add(linkTo(methodOn(PriceSetController.class).updatePrice(id, null, null))
                    .withRel("update-price"));
            model.add(linkTo(methodOn(PriceSetController.class).removePrice(id, null))
                    .withRel("remove-price"));
        }

        return model;
    }
}
