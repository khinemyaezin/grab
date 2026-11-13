package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerMerchantApiLinks;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerMerchantApplicationApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MerchantModelAssembler implements RepresentationModelAssembler<MerchantResponse, EntityModel<MerchantResponse>> {

    @Override
    public EntityModel<MerchantResponse> toModel(MerchantResponse merchant) {
        return assemble(merchant, true);
    }

    public EntityModel<MerchantResponse> toCommandModel(MerchantResponse merchant) {
        return assemble(merchant, false);
    }

    private EntityModel<MerchantResponse> assemble(MerchantResponse merchant, boolean canonicalRead) {
        if (merchant == null) {
            return null;
        }
        EntityModel<MerchantResponse> model = EntityModel.of(merchant);
        if (merchant.merchantId() != null) {
            Link retrieval = SellerMerchantApiLinks.getMerchant(merchant.merchantId());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
            if (isEditableApplication(merchant.status())) {
                model.add(SellerMerchantApiLinks.updateProfile(merchant.merchantId()));
                model.add(SellerMerchantApplicationApiLinks.submitApplication(merchant.merchantId()));
            }
        }
        model.add(SellerMerchantApiLinks.listMyMerchants());
        return model;
    }

    private boolean isEditableApplication(String status) {
        if (status == null) {
            return false;
        }
        return "DRAFT".equalsIgnoreCase(status) || "CHANGES_REQUESTED".equalsIgnoreCase(status);
    }

    public CollectionModel<EntityModel<MerchantResponse>> listMyMerchantsModel(List<MerchantResponse> responses) {
        List<EntityModel<MerchantResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerMerchantApiLinks.listMyMerchants().expand();
        CollectionModel<EntityModel<MerchantResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        model.add(SellerMerchantApplicationApiLinks.startApplication());
        return model;
    }

    public CollectionModel<EntityModel<MerchantResponse>> reviewQueueModel(List<MerchantResponse> responses, String status) {
        List<EntityModel<MerchantResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerMerchantApiLinks.reviewQueue(status).expand();
        CollectionModel<EntityModel<MerchantResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        return model;
    }
}
