package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales-channels/root")
public class SellerSalesChannelRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.salesChannelRoot().withSelfRel());

        model.add(SellerSalesChannelApiLinks.listChannels(0, 20));
        model.add(SellerSalesChannelApiLinks.getChannel(null));
        model.add(SellerSalesChannelApiLinks.provisionMerchantWebsiteChannel());

        model.add(SellerPublishableKeyApiLinks.resolveKey(null));

        return ResponseEntity.ok(model);
    }
}
