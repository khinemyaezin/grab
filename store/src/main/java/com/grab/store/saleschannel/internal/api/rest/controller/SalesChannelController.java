package com.grab.store.saleschannel.internal.api.rest.controller;

import com.grab.store.saleschannel.internal.api.rest.assembler.SalesChannelModelAssembler;
import com.grab.store.saleschannel.internal.api.rest.dto.response.SalesChannelResponse;
import com.grab.store.saleschannel.internal.api.rest.service.SalesChannelQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales-channels/channels")
@RequiredArgsConstructor
public class SalesChannelController {

    private final SalesChannelQueryService salesChannelQueryService;
    private final SalesChannelModelAssembler salesChannelModelAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<SalesChannelResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<SalesChannelResponse> pagedResourcesAssembler
    ) {
        Page<SalesChannelResponse> page = salesChannelQueryService.list(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, salesChannelModelAssembler));
    }

    @GetMapping("/{salesChannelId}")
    public ResponseEntity<EntityModel<SalesChannelResponse>> get(@PathVariable String salesChannelId) {
        SalesChannelResponse response = salesChannelQueryService.get(salesChannelId);
        return ResponseEntity.ok(salesChannelModelAssembler.toModel(response));
    }
}
