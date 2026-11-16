package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerPublishableKeyApiLinks;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerSalesChannelApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SalesChannelModelAssembler implements RepresentationModelAssembler<SalesChannelResponse, EntityModel<SalesChannelResponse>> {

    @Override
    public EntityModel<SalesChannelResponse> toModel(SalesChannelResponse channel) {
        if (channel == null) {
            return null;
        }
        EntityModel<SalesChannelResponse> model = EntityModel.of(channel);
        if (channel.salesChannelId() != null) {
            model.add(SellerSalesChannelApiLinks.getChannel(channel.salesChannelId()).withSelfRel());
            model.add(SellerPublishableKeyApiLinks.createPublishableKey(channel.salesChannelId()));
        }
        model.add(SellerSalesChannelApiLinks.listChannels(0, 20));
        return model;
    }

    public CollectionModel<EntityModel<SalesChannelResponse>> listChannelsModel(List<SalesChannelResponse> responses, int page, int size) {
        List<EntityModel<SalesChannelResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerSalesChannelApiLinks.listChannels(page, size).expand();
        CollectionModel<EntityModel<SalesChannelResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        model.add(SellerSalesChannelApiLinks.provisionMerchantWebsiteChannel());
        return model;
    }
}
