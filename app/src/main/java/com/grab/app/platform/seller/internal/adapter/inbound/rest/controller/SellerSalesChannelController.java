package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SalesChannelModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SalesChannelProvisionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.ProvisionWebsiteChannelRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelProvisionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerSalesChannelCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerSalesChannelQueryService;
import com.grab.app.platform.seller.port.rest.SellerSalesChannelResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-channels")
public class SellerSalesChannelController implements SellerSalesChannelResource {

    private final SellerSalesChannelCommandService commandService;
    private final SellerSalesChannelQueryService queryService;
    private final SalesChannelModelAssembler modelAssembler;
    private final SalesChannelProvisionModelAssembler provisionModelAssembler;

    public SellerSalesChannelController(
            SellerSalesChannelCommandService commandService,
            SellerSalesChannelQueryService queryService,
            SalesChannelModelAssembler modelAssembler,
            SalesChannelProvisionModelAssembler provisionModelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
        this.provisionModelAssembler = provisionModelAssembler;
    }

    @PostMapping("/merchant-website")
    public ResponseEntity<EntityModel<SalesChannelProvisionResponse>> provisionMerchantWebsiteChannel(
            @Valid @RequestBody ProvisionWebsiteChannelRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        SalesChannelProvisionResponse response = commandService.provisionMerchantWebsiteChannel(merchantId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.salesChannelId())
                .toUri();
        return ResponseEntity.created(location).body(provisionModelAssembler.toModel(response));
    }

    @GetMapping("/{salesChannelId}")
    public ResponseEntity<EntityModel<SalesChannelResponse>> getChannel(
            @PathVariable String salesChannelId,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        return queryService.getChannelById(salesChannelId, merchantId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<SalesChannelResponse>>> listChannels(
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<EntityModel<SalesChannelResponse>> list = queryService.listChannels(merchantId, page, size).stream()
                .map(modelAssembler::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(list));
    }
}

