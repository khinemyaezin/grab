package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.AccessContextModelAssembler;
import com.grab.store.identity.internal.api.rest.assembler.AuthModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.response.AccessContextResponse;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.api.rest.service.AccessCommandService;
import com.grab.store.identity.internal.api.rest.service.AccessQueryService;
import com.grab.store.shared.security.util.AuthCookieHelper;
import com.grab.store.shared.security.SecurityPrincipal;
import jakarta.validation.constraints.NotBlank;
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
public class AccessContextController {
    private final AccessQueryService queryService;
    private final AccessCommandService commandService;
    private final AccessContextModelAssembler contextModelAssembler;
    private final AuthModelAssembler authModelAssembler;
    private final AuthCookieHelper authCookieHelper;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AccessContextResponse>>> listContexts(
            @RequestHeader(value = "X-Platform") @NotBlank String platformCode,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        List<EntityModel<AccessContextResponse>> contexts = queryService
                .listContexts(principal.getPlatformUserId(), platformCode)
                .stream()
                .map(contextModelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<AccessContextResponse>> model = CollectionModel.of(contexts);
        model.add(linkTo(methodOn(AccessContextController.class)
                .listContexts(platformCode, null))
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
