package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPublishableKeyController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSalesChannelController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelProvisionResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SalesChannelProvisionModelAssembler implements RepresentationModelAssembler<SalesChannelProvisionResponse, EntityModel<SalesChannelProvisionResponse>> {

    @Override
    public EntityModel<SalesChannelProvisionResponse> toModel(SalesChannelProvisionResponse provision) {
        if (provision == null) {
            return null;
        }
        EntityModel<SalesChannelProvisionResponse> model = EntityModel.of(provision);
        if (provision.salesChannelId() != null) {
            model.add(linkTo(methodOn(SellerSalesChannelController.class).getChannel(provision.salesChannelId(), null)).withSelfRel());
            model.add(linkTo(methodOn(SellerPublishableKeyController.class).createPublishableKey(provision.salesChannelId(), null)).withRel("create-publishable-key"));
        }
        model.add(linkTo(methodOn(SellerSalesChannelController.class).listChannels(null, 0, 20)).withRel("list-sales-channels"));
        return model;
    }
}

