package com.grab.store.merchant.internal.api.rest.assembler;

import com.grab.store.merchant.internal.api.rest.controller.StorefrontController;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.merchant.domain.enums.StorefrontStatus;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@MerchantEnabled
public class StorefrontModelAssembler
        implements RepresentationModelAssembler<StorefrontResponse, EntityModel<StorefrontResponse>> {

    @Override
    public EntityModel<StorefrontResponse> toModel(StorefrontResponse response) {
        EntityModel<StorefrontResponse> model = EntityModel.of(
                response,
                linkTo(methodOn(StorefrontController.class).get(response.storefrontId(), null))
                        .withSelfRel()
        );
        model.add(linkTo(methodOn(StorefrontController.class).list(null)).withRel("list-storefronts"));
        model.add(linkTo(methodOn(StorefrontController.class).create(null, null)).withRel("create-storefront"));

        StorefrontStatus status = StorefrontStatus.valueOf(response.status());
        if (status != StorefrontStatus.CLOSED) {
            model.add(linkTo(methodOn(StorefrontController.class)
                    .update(response.storefrontId(), null, null))
                    .withRel("update-storefront"));
        }
        if (status == StorefrontStatus.DRAFT) {
            model.add(linkTo(methodOn(StorefrontController.class)
                    .activate(response.storefrontId(), null))
                    .withRel("activate-storefront"));
        }
        if (status == StorefrontStatus.ACTIVE) {
            model.add(linkTo(methodOn(StorefrontController.class)
                    .suspend(response.storefrontId(), null, null))
                    .withRel("suspend-storefront"));
        }
        if (status == StorefrontStatus.SUSPENDED) {
            model.add(linkTo(methodOn(StorefrontController.class)
                    .reactivate(response.storefrontId(), null))
                    .withRel("reactivate-storefront"));
        }
        if (status != StorefrontStatus.CLOSED) {
            model.add(linkTo(methodOn(StorefrontController.class)
                    .close(response.storefrontId(), null, null))
                    .withRel("close-storefront"));
        }
        return model;
    }

    public CollectionModel<EntityModel<StorefrontResponse>> toCollectionModel(List<StorefrontResponse> responses) {
        CollectionModel<EntityModel<StorefrontResponse>> collection =
                RepresentationModelAssembler.super.toCollectionModel(responses);
        collection.add(linkTo(methodOn(StorefrontController.class).list(null)).withRel("list-storefronts"));
        collection.add(linkTo(methodOn(StorefrontController.class).create(null, null)).withRel("create-storefront"));
        return collection;
    }
}
