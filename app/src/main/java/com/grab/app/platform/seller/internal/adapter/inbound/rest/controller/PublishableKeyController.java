package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.port.rest.SellerRestApi;

import com.grab.app.platform.seller.internal.command.*;
import com.grab.app.platform.seller.internal.query.*;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.assembler.PublishableKeyModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.request.CreatePublishableKeyRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response.CreatedPublishableKeyResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response.PublishableKeyResponse;
import com.grab.app.platform.seller.internal.command.PublishableKeyCommandService;
import com.grab.app.platform.seller.internal.query.PublishableKeyQueryService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SalesChannelController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/sales-channels/channels/{salesChannelId}/publishable-keys")
@RequiredArgsConstructor
public class PublishableKeyController implements SellerRestApi {
    private final PublishableKeyCommandService commandService;
    private final PublishableKeyQueryService queryService;
    private final PublishableKeyModelAssembler assembler;

    @PostMapping
    public ResponseEntity<EntityModel<CreatedPublishableKeyResponse>> create(
            @PathVariable String salesChannelId,
            @Valid @RequestBody CreatePublishableKeyRequest request
    ) {
        CreatedPublishableKeyResponse response = commandService.create(salesChannelId, request);
        EntityModel<CreatedPublishableKeyResponse> model = EntityModel.of(
                response,
                linkTo(methodOn(PublishableKeyController.class).list(salesChannelId, null, null))
                        .withRel("list-publishable-keys"),
                linkTo(methodOn(PublishableKeyController.class).create(salesChannelId, null))
                        .withRel("create-publishable-key"),
                linkTo(methodOn(SalesChannelController.class).get(salesChannelId))
                        .withRel("get-sales-channel")
        );
        return ResponseEntity.status(201)
                .cacheControl(CacheControl.noStore())
                .body(model);
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<PublishableKeyResponse>>> list(
            @PathVariable String salesChannelId,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<PublishableKeyResponse> pagedResourcesAssembler
    ) {
        Page<PublishableKeyResponse> page = queryService.list(salesChannelId, pageable);
        PagedModel<EntityModel<PublishableKeyResponse>> model =
                pagedResourcesAssembler.toModel(page, assembler);
        model.add(linkTo(methodOn(PublishableKeyController.class).list(salesChannelId, null, null))
                .withRel("list-publishable-keys"));
        model.add(linkTo(methodOn(PublishableKeyController.class).create(salesChannelId, null))
                .withRel("create-publishable-key"));
        model.add(linkTo(methodOn(SalesChannelController.class).get(salesChannelId))
                .withRel("get-sales-channel"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(model);
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revoke(
            @PathVariable String salesChannelId,
            @PathVariable String keyId
    ) {
        commandService.revoke(salesChannelId, keyId);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
