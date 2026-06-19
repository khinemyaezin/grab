package com.grab.store.shared.security;

import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.enums.UserStatus;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.entity.ExternalEntitlementMappingEntity;
import com.identity.infrastructure.entity.ExternalIdentityEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.ExternalEntitlementMappingJpaRepository;
import com.identity.infrastructure.repository.jpa.ExternalIdentityJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityResolverAdapterTest {

    @Mock
    private UserJpaRepository users;
    @Mock
    private ExternalIdentityJpaRepository externalIdentities;
    @Mock
    private ExternalEntitlementMappingJpaRepository entitlementMappings;
    @Mock
    private LocalJwtProperties properties;

    @InjectMocks
    private IdentityResolverAdapter resolver;

    @BeforeEach
    void setUp() {
    }

    @Test
    void resolve_withActiveUser_shouldReturnAuthenticatedActor() {
        when(properties.issuer()).thenReturn("test-issuer");

        String userId = UUID.randomUUID().toString();
        ExternalPrincipal principal = new ExternalPrincipal("test-issuer", userId, Optional.of("test@example.com"), Set.of());

        UserEntity userEntity = new UserEntity();
        userEntity.setUuid(userId);
        userEntity.setEmail("test@example.com");
        userEntity.setStatus(UserStatus.ACTIVE);

        RoleEntity role = new RoleEntity();
        role.setCode("CUSTOMER");
        role.setActive(true);

        AuthorityEntity authority = new AuthorityEntity();
        authority.setCode("READ_PRODUCTS");
        authority.setActive(true);
        role.setAuthorities(Set.of(authority));

        userEntity.setRoles(Set.of(role));

        when(users.findByUuid(userId)).thenReturn(Optional.of(userEntity));

        AuthenticatedActor actor = resolver.resolve(principal);

        assertEquals(userId, actor.platformUserId());
        assertEquals("test-issuer", actor.issuer());
        assertEquals(userId, actor.subject());
        assertEquals(Set.of("CUSTOMER"), actor.roles());
        assertEquals(Set.of("READ_PRODUCTS"), actor.authorities());
    }

    @Test
    void resolve_withSuspendedUser_shouldRejectAuthentication() {
        when(properties.issuer()).thenReturn("test-issuer");

        String userId = UUID.randomUUID().toString();
        ExternalPrincipal principal = new ExternalPrincipal("test-issuer", userId, Optional.of("test@example.com"), Set.of());

        UserEntity userEntity = new UserEntity();
        userEntity.setStatus(UserStatus.SUSPENDED);

        when(users.findByUuid(userId)).thenReturn(Optional.of(userEntity));

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> resolver.resolve(principal));
        assertInstanceOf(IdentitySecurityError.AccountNotActive.class, exception.getMessageSource());
    }

    @Test
    void resolve_withMissingIdentity_shouldRejectAuthentication() {
        when(properties.issuer()).thenReturn("test-issuer");

        String userId = UUID.randomUUID().toString();
        ExternalPrincipal principal = new ExternalPrincipal("test-issuer", userId, Optional.of("test@example.com"), Set.of());

        when(users.findByUuid(userId)).thenReturn(Optional.empty());

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> resolver.resolve(principal));
        assertInstanceOf(IdentitySecurityError.IdentityNotLinked.class, exception.getMessageSource());
    }

    @Test
    void resolve_withExternalIdentity_shouldReturnAuthenticatedActor() {
        when(properties.issuer()).thenReturn("local-issuer");

        String externalIssuer = "external-issuer";
        String externalSubject = "ext-sub";
        ExternalPrincipal principal = new ExternalPrincipal(externalIssuer, externalSubject, Optional.of("test@example.com"), Set.of("ext-role"));

        UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setEmail("test@example.com");
        userEntity.setStatus(UserStatus.ACTIVE);

        ExternalIdentityEntity externalIdentity = new ExternalIdentityEntity();
        externalIdentity.setUser(userEntity);

        when(externalIdentities.findByIssuerAndSubject(externalIssuer, externalSubject)).thenReturn(Optional.of(externalIdentity));

        RoleEntity role = new RoleEntity();
        role.setCode("SELLER");
        role.setActive(true);

        ExternalEntitlementMappingEntity mapping = new ExternalEntitlementMappingEntity();
        mapping.setRole(role);

        when(entitlementMappings.findByIssuerAndEntitlementIn(externalIssuer, Set.of("ext-role"))).thenReturn(List.of(mapping));

        AuthenticatedActor actor = resolver.resolve(principal);

        assertEquals(externalIssuer, actor.issuer());
        assertEquals(externalSubject, actor.subject());
        assertEquals(Set.of("SELLER"), actor.roles());
    }
}
