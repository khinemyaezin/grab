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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler.AccessContextModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler.AuthModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessContextResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AuthResponse;
import com.grab.app.platform.seller.internal.command.AccessCommandService;
import com.grab.app.platform.seller.internal.query.AccessQueryService;
import com.grab.app.shared.security.util.AuthCookieHelper;
import com.grab.app.shared.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Validated
@RestController
@RequestMapping("/api/v1/identity/access-contexts")
@RequiredArgsConstructor
public class AccessContextController implements SellerRestApi {
    private final AccessQueryService queryService;
    private final AccessCommandService commandService;
    private final AccessContextModelAssembler contextModelAssembler;
    private final AuthModelAssembler authModelAssembler;
    private final AuthCookieHelper authCookieHelper;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AccessContextResponse>>> listContexts(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        List<AccessContextResponse> responses = queryService.listContexts(principal.getPlatformUserId());
        CollectionModel<EntityModel<AccessContextResponse>> model = contextModelAssembler.toCollectionModel(responses);
        model.add(linkTo(methodOn(AccessContextController.class)
                .listContexts(null))
                .withRel("list-access-contexts"));
        return ResponseEntity.ok(model);
    }

    @PostMapping("/{assignmentId}/select")
    public ResponseEntity<EntityModel<AuthResponse>> selectContext(
            @PathVariable String assignmentId,
            @CookieValue(name = "refreshToken", required = false) String currentRefreshToken,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        AuthResponse response = commandService.switchContext(assignmentId, currentRefreshToken, principal);
        return ResponseEntity.ok()
                .headers(authCookieHelper.createTokenCookies(response.accessToken(), response.refreshToken(), response.expiresInMs()))
                .body(authModelAssembler.toModel(response));
    }
}
