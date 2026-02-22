package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.SyncVariantsResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SyncVariantsModelAssembler implements RepresentationModelAssembler<SyncVariantsResponse, EntityModel<SyncVariantsResponse>> {

    @Override
    public EntityModel<SyncVariantsResponse> toModel(SyncVariantsResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products/" + response.productId() + "/variants").withSelfRel(),
                Link.of("/api/v1/products/" + response.productId()).withRel("product")
        );
    }
}
