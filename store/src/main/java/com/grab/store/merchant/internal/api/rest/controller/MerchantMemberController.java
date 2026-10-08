package com.grab.store.merchant.internal.api.rest.controller;

import com.grab.store.merchant.internal.api.rest.assembler.MerchantMemberModelAssembler;
import com.grab.store.merchant.internal.api.rest.dto.request.ChangeMerchantMemberRoleRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.InviteMerchantMemberRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.grab.store.merchant.internal.api.rest.service.MerchantMemberCommandService;
import com.grab.store.merchant.internal.api.rest.service.MerchantMemberQueryService;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.security.SecurityPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@MerchantEnabled
@RequestMapping("/api/v1/merchants/{merchantId}/members")
@RequiredArgsConstructor
public class MerchantMemberController {

    private final MerchantMemberCommandService commands;
    private final MerchantMemberQueryService queries;
    private final MerchantMemberModelAssembler assembler;

    @PostMapping("/invite")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> invite(
            @PathVariable String merchantId,
            @Valid @RequestBody InviteMerchantMemberRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorUserId = principal != null ? principal.getPlatformUserId() : null;
        MerchantMemberResponse response = commands.invite(merchantId, request, actorUserId);
        EntityModel<MerchantMemberResponse> model = assembler.toModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @PostMapping("/{memberId}/accept")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> accept(
            @PathVariable String merchantId,
            @PathVariable String memberId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorUserId = principal != null ? principal.getPlatformUserId() : null;
        MerchantMemberResponse response = commands.accept(merchantId, memberId, actorUserId);
        EntityModel<MerchantMemberResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @PutMapping("/{memberId}/role")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> changeRole(
            @PathVariable String merchantId,
            @PathVariable String memberId,
            @Valid @RequestBody ChangeMerchantMemberRoleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorUserId = principal != null ? principal.getPlatformUserId() : null;
        MerchantMemberResponse response = commands.changeRole(merchantId, memberId, request, actorUserId);
        EntityModel<MerchantMemberResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<EntityModel<MerchantMemberResponse>> remove(
            @PathVariable String merchantId,
            @PathVariable String memberId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String actorUserId = principal != null ? principal.getPlatformUserId() : null;
        MerchantMemberResponse response = commands.remove(merchantId, memberId, actorUserId);
        EntityModel<MerchantMemberResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantMemberResponse>>> list(
            @PathVariable String merchantId
    ) {
        List<MerchantMemberResponse> responses = queries.list(merchantId);
        CollectionModel<EntityModel<MerchantMemberResponse>> collection = assembler.toCollectionModel(responses, merchantId);
        return ResponseEntity.ok(collection);
    }
}
