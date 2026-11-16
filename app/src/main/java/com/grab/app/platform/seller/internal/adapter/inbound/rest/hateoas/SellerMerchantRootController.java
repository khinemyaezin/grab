package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants/root")
public class SellerMerchantRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.merchantRoot().withSelfRel());

        model.add(SellerMerchantApiLinks.listMyMerchants());
        model.add(SellerMerchantApiLinks.getMerchant(null));

        model.add(SellerMerchantApplicationApiLinks.startApplication());
        model.add(SellerMerchantApplicationApiLinks.getC2CApplication());
        model.add(SellerMerchantApplicationApiLinks.startC2CApplication());
        model.add(SellerMerchantApplicationApiLinks.getFirstPartyApplication());
        model.add(SellerMerchantApplicationApiLinks.startFirstPartyApplication());

        model.add(SellerStorefrontApiLinks.listStorefronts(null));
        model.add(SellerStorefrontApiLinks.getStorefront(null, null));
        model.add(SellerStorefrontApiLinks.createStorefront(null));

        model.add(SellerRegionApiLinks.countries());
        return ResponseEntity.ok(model);
    }
}
