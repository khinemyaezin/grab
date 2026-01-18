package com.grab.store.product.internal.api.rest.assembler;

import com.grab.store.product.internal.api.rest.dto.response.DeleteProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class DeleteProductModelAssembler
        implements RepresentationModelAssembler<DeleteProductResponse, EntityModel<DeleteProductResponse>> {

    @Override
    public EntityModel<DeleteProductResponse> toModel(DeleteProductResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products").withRel("products"),
                Link.of("/api/v1/products/build").withRel("build"),
                Link.of("/api/v1/products/combinations").withRel("combinations")
        );
    }
}
