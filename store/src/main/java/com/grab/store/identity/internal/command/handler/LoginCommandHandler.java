package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.store.identity.internal.command.AuthResult;
import com.grab.store.identity.internal.command.LoginCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.shared.security.LocalJwtProperties;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.service.TokenIssuer;
import com.identity.domain.service.TokenPair;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoginCommandHandler implements CommandHandler<LoginCommand, AuthResult> {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final PlatformIdentityResolver identityResolver;
    private final LocalJwtProperties jwtProperties;

    @Override
    @IdentityTransactional
    public AuthResult handle(LoginCommand command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(this::invalidCredentials);
        if (!user.isActive()
                || user.getPasswordHash().isEmpty()
                || !passwordHasher.verify(command.password(), user.getPasswordHash().orElseThrow())) {
            throw invalidCredentials();
        }

        AuthenticatedActor actor = identityResolver.resolve(new ExternalPrincipal(
                jwtProperties.issuer(),
                user.getId().getValue(),
                Optional.of(user.getEmail().value()),
                user.getRoleCodes()
        ));
        TokenPair tokenPair = tokenIssuer.issue(actor);
        return new AuthResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.expiresInMs(),
                actor.platformUserId(),
                actor.email(),
                actor.roles(),
                user.getStatus().name()
        );
    }

    @Override
    public Class<LoginCommand> getCommandType() {
        return LoginCommand.class;
    }

    private IdentityAuthenticationException invalidCredentials() {
        return new IdentityAuthenticationException(
                new IdentitySecurityError.InvalidCredentials(),
                "Invalid email or password"
        );
    }
}
