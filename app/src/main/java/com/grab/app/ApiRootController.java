package com.grab.app;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerApiRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.CartRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.RegionRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SalesChannelRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.StorefrontQueryRootController;
import com.grab.app.shared.sse.EventStreamController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1")
public class ApiRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(ApiRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(SellerApiRootController.class).root()).withRel("get-seller-root"));
        model.add(linkTo(methodOn(SalesChannelRootController.class).root()).withRel("get-sales-channel-root"));
        model.add(linkTo(methodOn(StorefrontQueryRootController.class).root()).withRel("get-storefront-query-root"));
        model.add(linkTo(methodOn(CartRootController.class).root()).withRel("get-cart-root"));
        model.add(linkTo(methodOn(RegionRootController.class).root()).withRel("get-region-root"));
        model.add(linkTo(methodOn(EventStreamController.class).stream(null, null, null)).withRel("event-stream"));
        return ResponseEntity.ok(model);
    }
}
