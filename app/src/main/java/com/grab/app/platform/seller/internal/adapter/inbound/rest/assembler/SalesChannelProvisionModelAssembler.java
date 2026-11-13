package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelProvisionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerPublishableKeyApiLinks;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerSalesChannelApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SalesChannelProvisionModelAssembler implements RepresentationModelAssembler<SalesChannelProvisionResponse, EntityModel<SalesChannelProvisionResponse>> {

    @Override
    public EntityModel<SalesChannelProvisionResponse> toModel(SalesChannelProvisionResponse provision) {
        if (provision == null) {
            return null;
        }
        EntityModel<SalesChannelProvisionResponse> model = EntityModel.of(provision);
        if (provision.salesChannelId() != null) {
            model.add(SellerSalesChannelApiLinks.getChannel(provision.salesChannelId()));
            model.add(SellerPublishableKeyApiLinks.createPublishableKey(provision.salesChannelId()));
        }
        model.add(SellerSalesChannelApiLinks.listChannels(0, 20));
        return model;
    }
}

