package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.RoleModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.request.CreateRoleRequest;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.api.rest.dto.response.SearchRolesResponse;
import com.grab.store.identity.internal.api.rest.service.RoleCommandService;
import com.grab.store.identity.internal.api.rest.service.RoleQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/identity/admin/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleCommandService commandService;
    private final RoleQueryService queryService;
    private final RoleModelAssembler roleModelAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<RoleResponse>>> listRoles(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<RoleResponse> pagedAssembler
    ) {
        Page<RoleResponse> page = queryService.listRoles(pageable);
        PagedModel<EntityModel<RoleResponse>> model = pagedAssembler.toModel(page, roleModelAssembler);
        model.add(linkTo(methodOn(RoleAdminController.class)
                .listRoles(null, null))
                .withRel("list-roles"));
        model.add(linkTo(methodOn(RoleAdminController.class)
                .createRole(null))
                .withRel("create-role"));
        return ResponseEntity.ok(model);
    }

    @PostMapping
    public ResponseEntity<EntityModel<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        RoleResponse response = commandService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(roleModelAssembler.toModel(response));
    }

    @PutMapping("/{role}/authorities/{authority}")
    public ResponseEntity<EntityModel<RoleResponse>> grantAuthority(
            @PathVariable String role,
            @PathVariable String authority
    ) {
        return ResponseEntity.ok(roleModelAssembler.toModel(
                commandService.manageAuthority(role, authority, true)
        ));
    }

    @DeleteMapping("/{role}/authorities/{authority}")
    public ResponseEntity<EntityModel<RoleResponse>> revokeAuthority(
            @PathVariable String role,
            @PathVariable String authority
    ) {
        return ResponseEntity.ok(roleModelAssembler.toModel(
                commandService.manageAuthority(role, authority, false)
        ));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<CollectionModel<SearchRolesResponse>> suggestRoles(
            @RequestParam String name
    ) {
        List<SearchRolesResponse> responses = queryService.searchRoles(name);
        CollectionModel<SearchRolesResponse> collectionModel = CollectionModel.of(responses);
        collectionModel.add(linkTo(methodOn(RoleAdminController.class).suggestRoles(name)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }
}
