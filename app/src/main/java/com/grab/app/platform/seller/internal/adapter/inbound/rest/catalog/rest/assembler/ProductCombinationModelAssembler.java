package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.ProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.VariationMatrixResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductCombinationModelAssembler
        implements RepresentationModelAssembler<VariationMatrixResponse, EntityModel<VariationMatrixResponse>> {

    @Override
    public EntityModel<VariationMatrixResponse> toModel(VariationMatrixResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getVariationMatrix(null)).withRel("generate-variation-matrix")
        );
    }
}
