package com.grab.store.pricing.internal.api.rest.assembler;

import com.grab.store.pricing.internal.api.rest.controller.PriceSetController;
import com.grab.store.pricing.internal.api.rest.dto.response.CalculatedPriceSetResponse;
import com.grab.store.pricing.internal.config.PricingEnabled;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@PricingEnabled
public class CalculatedPriceSetModelAssembler
        implements RepresentationModelAssembler<CalculatedPriceSetResponse, EntityModel<CalculatedPriceSetResponse>> {

    @Override
    public EntityModel<CalculatedPriceSetResponse> toModel(CalculatedPriceSetResponse response) {
        EntityModel<CalculatedPriceSetResponse> model = EntityModel.of(response);
        if (response.id() != null) {
            model.add(linkTo(methodOn(PriceSetController.class).getPriceSet(response.id()))
                    .withRel("get-price-set"));
        }
        model.add(linkTo(methodOn(PriceSetController.class).calculatePrices(null)).withRel("calculate-prices"));
        return model;
    }
}
