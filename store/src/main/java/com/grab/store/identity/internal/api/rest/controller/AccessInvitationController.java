package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.AccessAssignmentModelAssembler;
import com.grab.store.identity.internal.api.rest.assembler.AccessInvitationModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.request.AcceptAccessInvitationRequest;
import com.grab.store.identity.internal.api.rest.dto.request.CreateAccessInvitationRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import com.grab.store.identity.internal.api.rest.dto.response.AccessInvitationResponse;
import com.grab.store.identity.internal.api.rest.service.AccessInvitationCommandService;
import com.grab.store.shared.security.SecurityPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/access-invitations")
@RequiredArgsConstructor
public class AccessInvitationController {
    private final AccessInvitationCommandService commandService;
    private final AccessInvitationModelAssembler invitationModelAssembler;
    private final AccessAssignmentModelAssembler assignmentModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<AccessInvitationResponse>> create(
            @Valid @RequestBody CreateAccessInvitationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        AccessInvitationResponse response = commandService.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationModelAssembler.toModel(response));
    }

    @PostMapping("/accept")
    public ResponseEntity<EntityModel<AccessAssignmentResponse>> accept(
            @Valid @RequestBody AcceptAccessInvitationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        AccessAssignmentResponse response = commandService.accept(request, principal);
        return ResponseEntity.ok(assignmentModelAssembler.toModel(response));
    }

    @PostMapping("/{invitationId}/cancel")
    public ResponseEntity<EntityModel<AccessInvitationResponse>> cancel(
            @PathVariable String invitationId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        AccessInvitationResponse response = commandService.cancel(invitationId, principal);
        return ResponseEntity.ok(invitationModelAssembler.toModel(response));
    }
}
