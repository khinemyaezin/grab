package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SellerApiRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.apiRoot());

        model.add(SellerDiscoveryApiLinks.catalogRoot());
        model.add(SellerDiscoveryApiLinks.inventoryRoot());
        model.add(SellerDiscoveryApiLinks.merchantRoot());
        model.add(SellerDiscoveryApiLinks.pricingRoot());
        model.add(SellerDiscoveryApiLinks.salesChannelRoot());
        model.add(SellerDiscoveryApiLinks.workflowRoot());
        model.add(SellerDiscoveryApiLinks.authRoot());

        model.add(SellerDiscoveryApiLinks.regionRoot());
        return ResponseEntity.ok(model);
    }
}
