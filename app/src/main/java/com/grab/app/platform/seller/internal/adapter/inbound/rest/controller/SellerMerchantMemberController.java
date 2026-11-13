package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantMemberModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.InviteMemberRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMemberRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/members")
@RequiredArgsConstructor
public class SellerMerchantMemberController {

    private final SellerMerchantCommandService commandService;
    private final SellerMerchantQueryService queryService;
    private final MerchantMemberModelAssembler modelAssembler;

    @PostMapping("/invite")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> inviteMember(
            @PathVariable String merchantId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantMemberResponse response = commandService.inviteMember(
                merchantId, principal.getPlatformUserId(), request);
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
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantMemberResponse response = commandService.acceptInvitation(
                merchantId, memberId, principal.getPlatformUserId());
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @PatchMapping("/{memberId}/role")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> updateRole(
            @PathVariable String merchantId,
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantMemberResponse response = commandService.updateMemberRole(
                merchantId, memberId, principal.getPlatformUserId(), request);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String merchantId,
            @PathVariable String memberId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        commandService.removeMember(merchantId, memberId, principal.getPlatformUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantMemberResponse>>> listMembers(
            @PathVariable String merchantId) {
        List<MerchantMemberResponse> results = queryService.listMembers(merchantId);
        return ResponseEntity.ok(modelAssembler.listMembersModel(results, merchantId));
    }
}
