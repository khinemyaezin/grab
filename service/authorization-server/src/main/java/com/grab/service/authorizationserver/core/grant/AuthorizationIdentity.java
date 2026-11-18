package com.grab.service.authorizationserver.core.grant;

import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.AccountUnavailableException;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.Subject;
import com.grab.service.authorizationserver.port.outbound.AssignmentIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AssignmentIdentityPort.State;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.ASSIGNMENT;
import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.MERCHANT_REVISION;
import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.SCOPE_ID;
import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.SCOPE_KEY;
import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.STOREFRONT_REVISION;
import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.VERSION;
import static com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport.version;

@Component
public class AuthorizationIdentity {
    private final AccountIdentityPort account;
    private final AssignmentIdentityPort assignments;
    private final AuthorizationClientPolicies clients;
    private final MeterRegistry metrics;

    public AuthorizationIdentity(AccountIdentityPort account, AssignmentIdentityPort assignments,
                                 AuthorizationClientPolicies clients, MeterRegistry metrics) {
        this.account = account;
        this.assignments = assignments;
        this.clients = clients;
        this.metrics = metrics;
    }

    public Identity initial(OAuth2Authorization authorization, String clientId) {
        return resolve(authorization, clientId, null, true);
    }

    public Identity live(OAuth2Authorization authorization, String clientId) {
        String assignment = authorization.getAttribute(ASSIGNMENT);
        Identity identity = resolve(authorization, clientId, assignment, false);
        if (identity == null || identity.subject().authenticationVersion() != version(authorization, VERSION)) {
            return null;
        }
        State state = identity.assignment();
        if (state == null) {
            return authorization.getAttribute(MERCHANT_REVISION) == null ? identity : null;
        }
        if (state.merchantRevision() != version(authorization, MERCHANT_REVISION)
                || state.storefrontRevision() != version(authorization, STOREFRONT_REVISION)
                || !Objects.equals(assignment, state.assignmentId())
                || !Objects.equals(authorization.getAttribute(SCOPE_KEY), state.scopeKey())
                || !Objects.equals(authorization.getAttribute(SCOPE_ID), state.scopeId())) {
            return null;
        }
        return identity;
    }

    public Identity forAssignment(OAuth2Authorization authorization, String clientId, String assignment) {
        return resolve(authorization, clientId, assignment, false);
    }

    private Identity resolve(OAuth2Authorization authorization, String clientId, String assignment, boolean initial) {
        try {
            String subject = authorization.getPrincipalName();
            if (clients.hasAssignmentContext(clientId)) {
                State state = assignments.resolve(subject, assignment, initial);
                return state == null ? null : new Identity(state.subject(), state);
            }
            Subject state = account.resolve(subject);
            return state == null ? null : new Identity(state, null);
        } catch (AccountUnavailableException exception) {
            metrics.counter("authorization.oidc.account.unavailable").increment();
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }
    }

    public OAuth2Authorization withState(OAuth2Authorization authorization, Identity identity) {
        State assignment = identity.assignment();
        long version = identity.subject().authenticationVersion();
        return OAuth2Authorization.from(authorization)
                .attribute(VERSION, version)
                .attributes(attributes -> {
                    attributes.remove(ASSIGNMENT);
                    attributes.remove(SCOPE_KEY);
                    attributes.remove(SCOPE_ID);
                    attributes.remove(MERCHANT_REVISION);
                    attributes.remove(STOREFRONT_REVISION);
                    if (assignment != null) {
                        attributes.put(MERCHANT_REVISION, assignment.merchantRevision());
                        attributes.put(STOREFRONT_REVISION, assignment.storefrontRevision());
                        if (assignment.assignmentId() != null) {
                            attributes.put(ASSIGNMENT, assignment.assignmentId());
                            attributes.put(SCOPE_KEY, assignment.scopeKey());
                            attributes.put(SCOPE_ID, assignment.scopeId());
                        }
                    }
                }).build();
    }

    public record Identity(Subject subject, State assignment) {}
}
