package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantMemberModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.InviteMemberRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMemberRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantQueryService;
import com.grab.app.platform.seller.port.rest.SellerMerchantResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/members")
public class SellerMerchantMemberController implements SellerMerchantResource.Members {

    private final SellerMerchantCommandService commandService;
    private final SellerMerchantQueryService queryService;
    private final MerchantMemberModelAssembler modelAssembler;

    public SellerMerchantMemberController(
            SellerMerchantCommandService commandService,
            SellerMerchantQueryService queryService,
            MerchantMemberModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping("/invite")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> inviteMember(
            @PathVariable String merchantId,
            @Valid @RequestBody InviteMemberRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        MerchantMemberResponse response = commandService.inviteMember(merchantId, actorId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.memberId())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
    }

    @PostMapping("/{memberId}/accept")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> acceptInvitation(
            @PathVariable String merchantId,
            @PathVariable String memberId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        MerchantMemberResponse response = commandService.acceptInvitation(merchantId, memberId, actorId);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @PatchMapping("/{memberId}/role")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> updateRole(
            @PathVariable String merchantId,
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        MerchantMemberResponse response = commandService.updateMemberRole(merchantId, memberId, actorId, request);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String merchantId,
            @PathVariable String memberId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        commandService.removeMember(merchantId, memberId, actorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantMemberResponse>>> listMembers(@PathVariable String merchantId) {
        List<MerchantMemberResponse> results = queryService.listMembers(merchantId);
        List<EntityModel<MerchantMemberResponse>> models = results.stream().map(modelAssembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(models));
    }
}
