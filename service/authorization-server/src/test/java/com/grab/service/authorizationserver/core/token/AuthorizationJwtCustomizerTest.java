package com.grab.service.authorizationserver.core.token;

import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;
import com.grab.service.authorizationserver.config.AuthorizationClientProperties;
import com.grab.service.authorizationserver.config.AuthorizationClientProperties.Client;
import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.core.user.SelectedContext;
import com.grab.service.contract.message.account.scim.GrabOidcClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationJwtCustomizerTest {
    private AuthorizationSigningKeys keys;
    private AuthorizationClientPolicies policies;
    private IamUserRepository users;
    private AuthorizationJwtCustomizer customizer;
    private RegisteredClient registeredClient;

    @BeforeEach
    void setUp() {
        keys = new AuthorizationSigningKeys("test-key-id", (selector, context) -> List.of());

        String hash = "{bcrypt}" + new BCryptPasswordEncoder(4).encode("secret");
        Client client = new Client(
                "seller-bff",
                "Seller BFF",
                hash,
                List.of("https://seller.example.com/login/oauth2/code/seller-bff"),
                List.of(),
                List.of("openid"),
                "https://api.example.com",
                null,
                null,
                null
        );
        AuthorizationClientProperties clientProperties = new AuthorizationClientProperties(List.of(client));
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");
        policies = new AuthorizationClientPolicies(clientProperties, environment);

        users = mock(IamUserRepository.class);
        customizer = new AuthorizationJwtCustomizer(keys, policies, users);

        registeredClient = RegisteredClient.withId("reg-1")
                .clientId("seller-bff")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://seller.example.com/login/oauth2/code/seller-bff")
                .scope("openid")
                .build();
    }

    @Test
    void customizesAccessTokenWithSelectedContext() {
        SelectedContext selected = new SelectedContext(
                "assign-100",
                "plat-user-1",
                "merchant.account",
                "merchant-888",
                "MERCHANT_OWNER",
                "Acme Enterprise",
                Set.of("catalog:read", "catalog:write"),
                false
        );

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName("iam-user-1")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .attribute(AuthorizationJwtCustomizer.VERSION, 2L)
                .build();
        authorization = selected.storeIn(authorization);

        JwsHeader.Builder jwsHeader = JwsHeader.with(() -> "RS256");
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();

        JwtEncodingContext context = JwtEncodingContext.with(jwsHeader, claims)
                .registeredClient(registeredClient)
                .principal(UsernamePasswordAuthenticationToken.authenticated("iam-user-1", null, List.of()))
                .authorization(authorization)
                .authorizedScopes(Set.of("openid"))
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        customizer.customize(context);

        JwtClaimsSet builtClaims = context.getClaims().build();
        assertThat(builtClaims.getAudience()).containsExactly("https://api.example.com");
        assertThat(builtClaims.<Long>getClaim(AuthorizationJwtCustomizer.VERSION)).isEqualTo(2L);
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.PLATFORM_USER_ID)).isEqualTo("plat-user-1");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.ASSIGNMENT_ID)).isEqualTo("assign-100");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.SCOPE_KEY)).isEqualTo("merchant.account");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.SCOPE_ID)).isEqualTo("merchant-888");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.ROLE)).isEqualTo("MERCHANT_OWNER");
        assertThat(builtClaims.<List<String>>getClaim(GrabOidcClaims.AUTHORITIES))
                .containsExactlyInAnyOrder("catalog:read", "catalog:write");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.ONBOARDING_SCOPE)).isNull();
    }

    @Test
    void customizesAccessTokenWithOnboardingWhenContextIsOnboarding() {
        SelectedContext selected = SelectedContext.onboarding("plat-user-2");

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName("iam-user-2")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();
        authorization = selected.storeIn(authorization);

        JwsHeader.Builder jwsHeader = JwsHeader.with(() -> "RS256");
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();

        JwtEncodingContext context = JwtEncodingContext.with(jwsHeader, claims)
                .registeredClient(registeredClient)
                .principal(UsernamePasswordAuthenticationToken.authenticated("iam-user-2", null, List.of()))
                .authorization(authorization)
                .authorizedScopes(Set.of("openid"))
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        customizer.customize(context);

        JwtClaimsSet builtClaims = context.getClaims().build();
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.ONBOARDING_SCOPE)).isEqualTo("true");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.PLATFORM_USER_ID)).isEqualTo("plat-user-2");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.SCOPE_ID)).isNull();
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.ASSIGNMENT_ID)).isNull();
    }

    @Test
    void customizesIdTokenWithSelectedContextAndUserDetails() {
        IamUser user = new IamUser("iam-user-3", "john_doe", "john@example.com", "hash",
                "John", "Doe", true, "plat-3", 1L, Set.of(), Instant.now(), Instant.now());
        when(users.findById("iam-user-3")).thenReturn(Optional.of(user));

        SelectedContext selected = new SelectedContext(
                "assign-200",
                "plat-3",
                "merchant.storefront",
                "store-555",
                "STORE_MANAGER",
                "Central Store",
                Set.of("inventory:edit"),
                false
        );

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName("iam-user-3")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();
        authorization = selected.storeIn(authorization);

        JwsHeader.Builder jwsHeader = JwsHeader.with(() -> "RS256");
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();

        JwtEncodingContext context = JwtEncodingContext.with(jwsHeader, claims)
                .registeredClient(registeredClient)
                .principal(UsernamePasswordAuthenticationToken.authenticated("iam-user-3", null, List.of()))
                .authorization(authorization)
                .authorizedScopes(Set.of("openid"))
                .tokenType(new OAuth2TokenType(OidcParameterNames.ID_TOKEN))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        customizer.customize(context);

        JwtClaimsSet builtClaims = context.getClaims().build();
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.PLATFORM_USER_ID)).isEqualTo("plat-3");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.SCOPE_KEY)).isEqualTo("merchant.storefront");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.SCOPE_ID)).isEqualTo("store-555");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.ROLE)).isEqualTo("STORE_MANAGER");
        assertThat(builtClaims.<String>getClaim(GrabOidcClaims.EMAIL)).isEqualTo("john@example.com");
        assertThat(builtClaims.<String>getClaim("given_name")).isEqualTo("John");
        assertThat(builtClaims.<String>getClaim("family_name")).isEqualTo("Doe");
        assertThat(builtClaims.<String>getClaim("name")).isEqualTo("John Doe");
    }
}
