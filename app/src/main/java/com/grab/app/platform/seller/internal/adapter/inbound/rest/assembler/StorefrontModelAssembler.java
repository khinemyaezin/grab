package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerStorefrontApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StorefrontModelAssembler implements RepresentationModelAssembler<StorefrontResponse, EntityModel<StorefrontResponse>> {

    @Override
    public EntityModel<StorefrontResponse> toModel(StorefrontResponse storefront) {
        return assemble(storefront, true);
    }

    public EntityModel<StorefrontResponse> toCommandModel(StorefrontResponse storefront) {
        return assemble(storefront, false);
    }

    private EntityModel<StorefrontResponse> assemble(StorefrontResponse storefront, boolean canonicalRead) {
        if (storefront == null) {
            return null;
        }
        EntityModel<StorefrontResponse> model = EntityModel.of(storefront);
        if (storefront.merchantId() != null && storefront.storefrontId() != null) {
            String merchantId = storefront.merchantId();
            String storefrontId = storefront.storefrontId();
            Link retrieval = SellerStorefrontApiLinks.getStorefront(merchantId, storefrontId);
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
            model.add(SellerStorefrontApiLinks.listStorefronts(merchantId));
            model.add(SellerStorefrontApiLinks.createStorefront(merchantId));

            String status = storefront.status();
            if (!"CLOSED".equalsIgnoreCase(status)) {
                model.add(SellerStorefrontApiLinks.updateStorefront(merchantId, storefrontId));
                model.add(SellerStorefrontApiLinks.closeStorefront(merchantId, storefrontId));
            }
            if ("DRAFT".equalsIgnoreCase(status)) {
                model.add(SellerStorefrontApiLinks.activateStorefront(merchantId, storefrontId));
            }
        }
        return model;
    }

    public CollectionModel<EntityModel<StorefrontResponse>> listStorefrontsModel(List<StorefrontResponse> responses, String merchantId) {
        List<EntityModel<StorefrontResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerStorefrontApiLinks.listStorefronts(merchantId).expand();
        CollectionModel<EntityModel<StorefrontResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        model.add(SellerStorefrontApiLinks.createStorefront(merchantId));
        return model;
    }
}
