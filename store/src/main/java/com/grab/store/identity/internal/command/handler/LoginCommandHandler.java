package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.security.*;
import com.grab.store.identity.internal.command.*;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.shared.security.LocalJwtProperties;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.*;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoginCommandHandler implements CommandHandler<LoginCommand, AuthResult> {
    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenIssuer tokenIssuer;
    private final PlatformIdentityResolver resolver;
    private final LocalJwtProperties jwt;

    @Override
    @IdentityTransactional
    public AuthResult handle(LoginCommand command) {
        var user = users.findByEmail(new Email(command.email())).orElseThrow(() -> invalid());
        if (!user.isActive() || user.getPasswordHash().isEmpty() || !hasher.verify(command.password(), user.getPasswordHash().orElseThrow()))
            throw invalid();
        AuthenticatedActor actor = resolver.resolve(new ExternalPrincipal(jwt.issuer(), user.getId().getValue(), Optional.of(user.getEmail().value()), user.getRoleCodes()));
        TokenPair pair = tokenIssuer.issue(actor);
        return new AuthResult(pair.accessToken(), pair.refreshToken(), pair.expiresInMs(), actor.platformUserId(), actor.email(), actor.roles(), user.getStatus().name());
    }

    private IdentityAuthenticationException invalid() {
        return new IdentityAuthenticationException(new IdentitySecurityError.InvalidCredentials(), "Invalid email or password");
    }

    @Override
    public Class<LoginCommand> getCommandType() {
        return LoginCommand.class;
    }
}
