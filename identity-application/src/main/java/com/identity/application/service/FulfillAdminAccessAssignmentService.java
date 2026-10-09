package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.FulfillAdminAccessAssignmentCommand;
import com.identity.application.port.inbound.FulfillAdminAccessAssignmentUseCase;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Role;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class FulfillAdminAccessAssignmentService implements FulfillAdminAccessAssignmentUseCase {
    private static final Logger log = Loggers.getLogger(FulfillAdminAccessAssignmentService.class);

    private final UserRepository users;
    private final RoleRepository roles;
    private final AccessAssignmentRepository assignments;
    private final IdGenerator ids;

    @Override
    public AccessAssignmentResult execute(FulfillAdminAccessAssignmentCommand command) {
        log.info("Fulfilling admin access assignment for userId={} merchantId={} role={}",
                command.applicantUserId().getValue(), command.merchantId().getValue(), command.roleCode());

        users.findById(command.applicantUserId()).orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(command.applicantUserId().getValue()),
                "User not found"
        ));

        Role role = roles.findByCode(command.roleCode()).orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.RoleNotFound(command.roleCode()),
                "Role not found"
        ));

        role.requireAssignable();

        AccessScope scope = AccessScope.from(command.scopeKey(), command.merchantId().getValue());
        Instant now = Instant.now();

        List<AccessAssignment> current = assignments.findCurrentByUserAndScope(command.applicantUserId(), scope);
        for (AccessAssignment assignment : current) {
            if (assignment.getRoleCode().equalsIgnoreCase(command.roleCode()) && assignment.isEffectiveAt(now)) {
                log.info("AccessAssignment already exists and active for user {} and role {}",
                        command.applicantUserId().getValue(), command.roleCode());
                return AccessAssignmentResult.from(assignment, now);
            }
        }

        AccessAssignment assignment = assignments.save(AccessAssignment.create(
                ids.generateId(),
                command.applicantUserId(),
                command.roleCode(),
                scope,
                null,
                null
        ));

        log.info("Created AccessAssignment {} for user {} with role {}",
                assignment.getId().getValue(), command.applicantUserId().getValue(), command.roleCode());
        return AccessAssignmentResult.from(assignment, now);
    }
}
