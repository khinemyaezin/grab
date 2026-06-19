package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.ManageAuthorityCommand;
import com.grab.store.identity.internal.command.RoleResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.repository.jpa.AuthorityJpaRepository;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ManageAuthorityCommandHandler implements CommandHandler<ManageAuthorityCommand, RoleResult> {
    private final RoleJpaRepository roles;
    private final AuthorityJpaRepository authorities;

    @Override
    @IdentityTransactional
    public RoleResult handle(ManageAuthorityCommand command) {
        RoleEntity r = roles.findByCode(command.roleCode())
                .orElseThrow(() -> new IdentityServiceException(new IdentityServiceError.RoleNotFound(command.roleCode()), "Role not found"));
        AuthorityEntity a = authorities.findByCode(command.authorityCode())
                .orElseThrow(() -> new IdentityServiceException(new IdentityServiceError.RoleNotFound(command.authorityCode()), "Authority not found"));
        
        if (command.assign()) r.getAuthorities().add(a);
        else r.getAuthorities().remove(a);
        
        r = roles.save(r);
        return new RoleResult(r.getCode(), r.getName(), r.getDescription(), r.isActive(), r.getAuthorities().stream().map(AuthorityEntity::getCode).collect(Collectors.toSet()));
    }

    @Override
    public Class<ManageAuthorityCommand> getCommandType() {
        return ManageAuthorityCommand.class;
    }
}
