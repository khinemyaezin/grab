package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSessionController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccessContextModelAssembler implements RepresentationModelAssembler<AccessContextResponse, EntityModel<AccessContextResponse>> {

    @Override
    public EntityModel<AccessContextResponse> toModel(AccessContextResponse response) {
        if (response == null) {
            return null;
        }
        EntityModel<AccessContextResponse> model = EntityModel.of(response);
        if (response.assignmentId() != null) {
            model.add(linkTo(methodOn(SellerSessionController.class).selectContext(response.assignmentId(), null, null)).withRel("select-context"));
        }
        return model;
    }

    public CollectionModel<EntityModel<AccessContextResponse>> toCollectionModel(List<AccessContextResponse> responses) {
        List<EntityModel<AccessContextResponse>> models = responses.stream().map(this::toModel).toList();
        CollectionModel<EntityModel<AccessContextResponse>> collection = CollectionModel.of(models);
        collection.add(linkTo(methodOn(SellerSessionController.class).listContexts(null)).withSelfRel());
        return collection;
    }
}
