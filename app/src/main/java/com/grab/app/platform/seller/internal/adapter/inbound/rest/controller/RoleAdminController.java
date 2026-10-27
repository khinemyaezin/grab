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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler.RoleModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler.SearchRolesModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.CreateRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.RoleResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.RoleSearchResponse;
import com.grab.app.platform.seller.internal.command.RoleCommandService;
import com.grab.app.platform.seller.internal.query.RoleQueryService;
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
public class RoleAdminController implements SellerRestApi {

    private final RoleCommandService commandService;
    private final RoleQueryService queryService;
    private final RoleModelAssembler roleModelAssembler;
    private final SearchRolesModelAssembler searchRolesModelAssembler;

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
        RoleResponse response = commandService.manageAuthority(role, authority, true);
        EntityModel<RoleResponse> model = roleModelAssembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/{role}/authorities/{authority}")
    public ResponseEntity<EntityModel<RoleResponse>> revokeAuthority(
            @PathVariable String role,
            @PathVariable String authority
    ) {
        RoleResponse response = commandService.manageAuthority(role, authority, false);
        EntityModel<RoleResponse> model = roleModelAssembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<CollectionModel<EntityModel<RoleSearchResponse>>> searchRoles(
            @RequestParam String name
    ) {
        List<RoleSearchResponse> responses = queryService.searchRoles(name);
        CollectionModel<EntityModel<RoleSearchResponse>> collectionModel =
                searchRolesModelAssembler.toCollectionModel(responses, name);
        return ResponseEntity.ok(collectionModel);
    }
}
