package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerSessionApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccessContextModelAssembler implements RepresentationModelAssembler<AccessContextResponse, EntityModel<AccessContextResponse>> {

    @Override
    public EntityModel<AccessContextResponse> toModel(AccessContextResponse response) {
        if (response == null) {
            return null;
        }
        EntityModel<AccessContextResponse> model = EntityModel.of(response);
        if (response.assignmentId() != null) {
            model.add(SellerSessionApiLinks.selectContext(response.assignmentId()));
        }
        return model;
    }

    public CollectionModel<EntityModel<AccessContextResponse>> toCollectionModel(List<AccessContextResponse> responses) {
        List<EntityModel<AccessContextResponse>> models = responses.stream().map(this::toModel).toList();
        CollectionModel<EntityModel<AccessContextResponse>> collection = CollectionModel.of(models);
        collection.add(SellerSessionApiLinks.listContexts().withSelfRel());
        collection.add(SellerSessionApiLinks.listContexts());
        return collection;
    }
}
