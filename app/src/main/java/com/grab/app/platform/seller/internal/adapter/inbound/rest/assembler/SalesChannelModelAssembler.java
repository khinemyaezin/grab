package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPublishableKeyController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSalesChannelController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SalesChannelModelAssembler implements RepresentationModelAssembler<SalesChannelResponse, EntityModel<SalesChannelResponse>> {

    @Override
    public EntityModel<SalesChannelResponse> toModel(SalesChannelResponse channel) {
        if (channel == null) {
            return null;
        }
        EntityModel<SalesChannelResponse> model = EntityModel.of(channel);
        if (channel.salesChannelId() != null) {
            model.add(linkTo(methodOn(SellerSalesChannelController.class).getChannel(channel.salesChannelId(), channel.merchantId())).withSelfRel());
            model.add(linkTo(methodOn(SellerPublishableKeyController.class).createPublishableKey(channel.salesChannelId(), null)).withRel("create-publishable-key"));
        }
        model.add(linkTo(methodOn(SellerSalesChannelController.class).listChannels(channel.merchantId(), 0, 20)).withRel("list-sales-channels"));
        return model;
    }
}

