package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.UserProfileModelAssembler;
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

    @GetMapping
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
    public ResponseEntity<EntityModel<UserProfileResponse>> getUser(@PathVariable String id) {
        UserProfileResponse response = queryService.getUser(id);
        return ResponseEntity.ok(userProfileModelAssembler.toModel(response));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<EntityModel<UserProfileResponse>> approveUser(@PathVariable String id) {
        UserProfileResponse response = commandService.changeStatus(id, UserStatus.ACTIVE);
        return ResponseEntity.ok(userProfileModelAssembler.toModel(response));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<EntityModel<UserProfileResponse>> suspendUser(@PathVariable String id) {
        UserProfileResponse response = commandService.changeStatus(id, UserStatus.SUSPENDED);
        return ResponseEntity.ok(userProfileModelAssembler.toModel(response));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<EntityModel<UserProfileResponse>> reactivateUser(@PathVariable String id) {
        UserProfileResponse response = commandService.changeStatus(id, UserStatus.ACTIVE);
        return ResponseEntity.ok(userProfileModelAssembler.toModel(response));
    }

    @PutMapping("/{id}/roles/{code}")
    public ResponseEntity<EntityModel<UserProfileResponse>> assignRole(
            @PathVariable String id,
            @PathVariable String code
    ) {
        UserProfileResponse response = commandService.assignRole(id, code, true);
        return ResponseEntity.ok(userProfileModelAssembler.toModel(response));
    }

    @DeleteMapping("/{id}/roles/{code}")
    public ResponseEntity<EntityModel<UserProfileResponse>> revokeRole(
            @PathVariable String id,
            @PathVariable String code
    ) {
        UserProfileResponse response = commandService.assignRole(id, code, false);
        return ResponseEntity.ok(userProfileModelAssembler.toModel(response));
    }
}
