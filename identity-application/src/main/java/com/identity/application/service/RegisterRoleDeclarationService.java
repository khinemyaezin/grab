package com.identity.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.role.RoleDeclaration;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import com.identity.application.model.write.RegisterRoleDeclarationResult;
import com.identity.application.port.inbound.RegisterRoleDeclarationUseCase;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.policy.ProtectedRoleReconciliationPolicy;
import com.identity.domain.policy.ProtectedRoleReconciliationPolicy.ReconciliationOutcome;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RegisterRoleDeclarationService implements RegisterRoleDeclarationUseCase {
    private static final Logger log = Loggers.getLogger(RegisterRoleDeclarationService.class);

    private final SecurityCatalogRepository catalogs;
    private final RoleRepository roles;
    private final AuthorityRepository authorities;

    @Override
    public RegisterRoleDeclarationResult execute(RegisterRoleDeclarationCommand command) {
        RoleDeclaration declaration = command.declaration();
        log.info("Processing RoleDeclaration for roleCode={} owner={} revision={}",
                declaration.roleCode(), declaration.owner(), declaration.declarationRevision());

        SecurityCatalog catalog = catalogs.loadForUpdate();
        ReconciliationOutcome outcome = ProtectedRoleReconciliationPolicy.evaluate(declaration, catalog, authorities);

        Optional<Role> roleOpt = roles.findByCode(declaration.roleCode());
        if (roleOpt.isEmpty()) {
            log.warn("System role {} not found in database", declaration.roleCode());
            return new RegisterRoleDeclarationResult(
                    declaration.roleCode(),
                    declaration.owner(),
                    declaration.declarationRevision(),
                    "FAILED",
                    "Role entity does not exist"
            );
        }

        Role role = roleOpt.get();
        if (outcome.isReconciled()) {
            role.reconcileSystemAuthorities(outcome.authorities(), true);
            roles.save(role);
            log.info("Successfully reconciled system role {} with {} authorities, assignable=true",
                    role.getCode(), outcome.authorities().size());
            return new RegisterRoleDeclarationResult(
                    declaration.roleCode(),
                    declaration.owner(),
                    declaration.declarationRevision(),
                    "RECONCILED",
                    null
            );
        } else {
            role.reconcileSystemAuthorities(null, false);
            roles.save(role);
            log.info("RoleDeclaration for {} waiting dependency: {}", declaration.roleCode(), outcome.reason());
            return new RegisterRoleDeclarationResult(
                    declaration.roleCode(),
                    declaration.owner(),
                    declaration.declarationRevision(),
                    "WAITING_DEPENDENCY",
                    outcome.reason()
            );
        }
    }
}
