package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.UserMutationModelAssembler;
import com.grab.store.identity.internal.api.rest.assembler.UserProfileModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.response.UserMutationResponse;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.api.rest.service.UserCommandService;
import com.grab.store.identity.internal.api.rest.service.UserQueryService;
import com.identity.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/identity/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserCommandService commandService;
    private final UserQueryService queryService;
    private final UserProfileModelAssembler userProfileModelAssembler;
    private final UserMutationModelAssembler userMutationModelAssembler;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<PagedModel<EntityModel<UserProfileResponse>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<UserProfileResponse> pagedAssembler
    ) {
        Page<UserProfileResponse> page = queryService.listUsers(pageable);
        PagedModel<EntityModel<UserProfileResponse>> model = pagedAssembler.toModel(
                page,
                userProfileModelAssembler
        );
        model.add(linkTo(methodOn(UserAdminController.class)
                .listUsers(null, null))
                .withRel("list-users"));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<EntityModel<UserProfileResponse>> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userProfileModelAssembler.toModel(queryService.getUser(id)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('USER_APPROVE')")
    public ResponseEntity<EntityModel<UserMutationResponse>> approveUser(@PathVariable String id) {
        return mutationResponse(commandService.changeStatus(id, UserStatus.ACTIVE));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('USER_SUSPEND')")
    public ResponseEntity<EntityModel<UserMutationResponse>> suspendUser(@PathVariable String id) {
        return mutationResponse(commandService.changeStatus(id, UserStatus.SUSPENDED));
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAuthority('USER_SUSPEND')")
    public ResponseEntity<EntityModel<UserMutationResponse>> reactivateUser(@PathVariable String id) {
        return mutationResponse(commandService.changeStatus(id, UserStatus.ACTIVE));
    }

    @PutMapping("/{id}/roles/{code}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<EntityModel<UserMutationResponse>> assignRole(
            @PathVariable String id,
            @PathVariable String code
    ) {
        return mutationResponse(commandService.assignRole(id, code, true));
    }

    @DeleteMapping("/{id}/roles/{code}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<EntityModel<UserMutationResponse>> revokeRole(
            @PathVariable String id,
            @PathVariable String code
    ) {
        return mutationResponse(commandService.assignRole(id, code, false));
    }

    private ResponseEntity<EntityModel<UserMutationResponse>> mutationResponse(UserMutationResponse response) {
        return ResponseEntity.ok(userMutationModelAssembler.toModel(response));
    }
}
