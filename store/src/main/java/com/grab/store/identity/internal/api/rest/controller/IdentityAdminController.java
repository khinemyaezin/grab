package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.dto.request.CreateRoleRequest;
import com.grab.store.identity.internal.api.rest.dto.response.*;
import com.grab.store.identity.internal.api.rest.service.IdentityAdminCommandService;
import com.grab.store.identity.internal.api.rest.service.IdentityAdminQueryService;
import com.grab.store.identity.internal.api.rest.assembler.RoleModelAssembler;
import com.grab.store.identity.internal.api.rest.assembler.UserProfileModelAssembler;
import com.identity.domain.enums.UserStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/identity/admin")
@RequiredArgsConstructor
public class IdentityAdminController {
    private final IdentityAdminCommandService commandService;
    private final IdentityAdminQueryService queryService;
    private final UserProfileModelAssembler userAssembler;
    private final RoleModelAssembler roleAssembler;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<PagedModel<EntityModel<UserProfileResponse>>> users(
            @PageableDefault(size = 20) Pageable p,
            PagedResourcesAssembler<UserProfileResponse> pagedAssembler
    ) {
        PagedModel<EntityModel<UserProfileResponse>> model = pagedAssembler.toModel(queryService.users(p), userAssembler);
        model.add(linkTo(methodOn(IdentityAdminController.class).users(null, null))
                .withRel("list-users"));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<EntityModel<UserProfileResponse>> getUser(
            @PathVariable String id) {
        return ResponseEntity.ok(userAssembler.toModel(queryService.profile(id)));
    }

    @PostMapping("/users/{id}/approve")
    @PreAuthorize("hasAuthority('USER_APPROVE')")
    public ResponseEntity<EntityModel<UserProfileResponse>> approve(
            @PathVariable String id) {
        return ResponseEntity.ok(userAssembler.toModel(commandService.status(id, UserStatus.ACTIVE)));
    }

    @PostMapping("/users/{id}/suspend")
    @PreAuthorize("hasAuthority('USER_SUSPEND')")
    public ResponseEntity<EntityModel<UserProfileResponse>> suspend(
            @PathVariable String id) {
        return ResponseEntity.ok(userAssembler.toModel(commandService.status(id, UserStatus.SUSPENDED)));
    }

    @PostMapping("/users/{id}/reactivate")
    @PreAuthorize("hasAuthority('USER_SUSPEND')")
    public ResponseEntity<EntityModel<UserProfileResponse>> reactivate(
            @PathVariable String id) {
        return ResponseEntity.ok(userAssembler.toModel(commandService.status(id, UserStatus.ACTIVE)));
    }

    @PutMapping("/users/{id}/roles/{code}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<EntityModel<UserProfileResponse>> assignRole(
            @PathVariable String id,
            @PathVariable String code) {
        return ResponseEntity.ok(userAssembler.toModel(commandService.assignRole(id, code, true)));
    }

    @DeleteMapping("/users/{id}/roles/{code}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<EntityModel<UserProfileResponse>> revokeRole(
            @PathVariable String id,
            @PathVariable String code) {
        return ResponseEntity.ok(userAssembler.toModel(commandService.assignRole(id, code, false)));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<CollectionModel<EntityModel<RoleResponse>>> roles() {
        return ResponseEntity.ok(roleAssembler.toCollectionModel(queryService.roles()));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<EntityModel<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest r) {
        return ResponseEntity.ok(roleAssembler.toModel(commandService.createRole(r)));
    }

    @PutMapping("/roles/{role}/authorities/{authority}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<EntityModel<RoleResponse>> grant(
            @PathVariable String role,
            @PathVariable String authority) {
        return ResponseEntity.ok(roleAssembler.toModel(commandService.authority(role, authority, true)));
    }

    @DeleteMapping("/roles/{role}/authorities/{authority}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<EntityModel<RoleResponse>> revoke(@PathVariable String role, @PathVariable String authority) {
        return ResponseEntity.ok(roleAssembler.toModel(commandService.authority(role, authority, false)));
    }
}
