package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.*;
import com.grab.store.identity.internal.command.*;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.shared.security.LocalJwtProperties;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.*;
import com.identity.domain.service.*;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.grab.store.identity.internal.exception.*;

import java.util.Optional;

@Component @RequiredArgsConstructor
public class RegisterUserCommandHandler implements CommandHandler<RegisterUserCommand, AuthResult> {
    private final UserRepository users; private final RoleRepository roles; private final PasswordHasher hasher;
    private final TokenIssuer tokenIssuer; private final PlatformIdentityResolver resolver; private final LocalJwtProperties jwt;
    @Override @IdentityTransactional public AuthResult handle(RegisterUserCommand command) {
        Email email = new Email(command.email()); if (users.existsByEmail(email)) throw new IdentityServiceException(new IdentityServiceError.EmailExists(email.value()), "Email already in use");
        String role = command.role().trim().toUpperCase(); if (roles.findByCode(role).isEmpty() || !java.util.Set.of("CUSTOMER","SELLER").contains(role)) throw new IdentityServiceException(new IdentityServiceError.InvalidRole(role), "Invalid registration role");
        User saved = users.save(User.createLocal(new CommonId(), email, hasher.hash(command.password()), role));
        if (!saved.isActive()) return new AuthResult(null, null, 0, saved.getId().getValue(), email.value(), saved.getRoleCodes(), saved.getStatus().name());
        AuthenticatedActor actor = resolver.resolve(new ExternalPrincipal(jwt.issuer(), saved.getId().getValue(), Optional.of(email.value()), saved.getRoleCodes()));
        TokenPair pair = tokenIssuer.issue(actor); return new AuthResult(pair.accessToken(), pair.refreshToken(), pair.expiresInMs(), actor.platformUserId(), actor.email(), actor.roles(), saved.getStatus().name());
    }
    @Override public Class<RegisterUserCommand> getCommandType() { return RegisterUserCommand.class; }
}
