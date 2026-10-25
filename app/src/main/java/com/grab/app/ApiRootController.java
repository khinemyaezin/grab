package com.grab.app;

import com.grab.app.shared.sse.EventStreamController;
import com.grab.app.productmerchandising.api.ProductMerchandisingApiLinks;
import com.grab.app.ordermanagement.api.OrderManagementApiLinks;
import com.grab.app.identityaccount.api.IdentityAccountApiLinks;
import com.grab.app.channeldiscovery.api.ChannelDiscoveryApiLinks;
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
        model.add(ProductMerchandisingApiLinks.catalogRoot());
        model.add(ProductMerchandisingApiLinks.inventoryRoot());
        model.add(IdentityAccountApiLinks.identityRoot());
        model.add(IdentityAccountApiLinks.merchantRoot());
        model.add(ProductMerchandisingApiLinks.pricingRoot());
        model.add(ChannelDiscoveryApiLinks.salesChannelRoot());
        model.add(ChannelDiscoveryApiLinks.storefrontQueryRoot());
        model.add(OrderManagementApiLinks.cartRoot());
        model.add(ChannelDiscoveryApiLinks.regionRoot());
        model.add(OrderManagementApiLinks.workflowsRoot());
        model.add(linkTo(methodOn(EventStreamController.class).stream(null, null, null)).withRel("event-stream"));
        return ResponseEntity.ok(model);
    }
}
