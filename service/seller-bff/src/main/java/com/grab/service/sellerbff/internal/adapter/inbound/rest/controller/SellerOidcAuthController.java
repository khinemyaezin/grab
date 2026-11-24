package com.grab.service.sellerbff.internal.adapter.inbound.rest.controller;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerCsrfResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
public class SellerOidcAuthController {
    private final SellerOidcProperties properties;

    @GetMapping("/csrf")
    public ResponseEntity<EntityModel<SellerCsrfResponse>> csrf(CsrfToken token) {
        SellerCsrfResponse value = new SellerCsrfResponse(token.getHeaderName(), token.getParameterName(), token.getToken());
        EntityModel<SellerCsrfResponse> model = EntityModel.of(value);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(model);
    }

    @GetMapping("/register-url")
    public ResponseEntity<EntityModel<Map<String, String>>> registerUrl() {
        String registerUrl = properties.issuer() + "/register";
        EntityModel<Map<String, String>> model = EntityModel.of(Map.of("url", registerUrl));
        return ResponseEntity.ok(model);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register() {
        URI registerUri = URI.create(properties.issuer() + "/register");
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(registerUri).build();
    }

    @GetMapping("/logout-url")
    public ResponseEntity<EntityModel<Map<String, String>>> logoutUrl() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        URI destination = resolveLogoutDestination(auth);
        EntityModel<Map<String, String>> model = EntityModel.of(Map.of("url", destination.toString()));
        return ResponseEntity.ok(model);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        URI destination = resolveLogoutDestination(auth);

        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, auth);

        return ResponseEntity.ok()
                .header("Referrer-Policy", "no-referrer")
                .cacheControl(CacheControl.noStore())
                .body(Map.of("logoutUrl", destination.toString()));
    }

    private URI resolveLogoutDestination(Authentication auth) {
        String idTokenHint = extractIdToken(auth);
        if (properties.logoutEndpoint() != null && idTokenHint != null && !idTokenHint.isBlank()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.logoutEndpoint())
                    .queryParam("id_token_hint", idTokenHint);
            if (properties.postLogout() != null) {
                builder.queryParam("post_logout_redirect_uri", properties.postLogout());
            }
            return builder.build().toUri();
        }
        return properties.postLogout() != null ? properties.postLogout() : URI.create("/");
    }

    private String extractIdToken(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof OidcUser oidcUser && oidcUser.getIdToken() != null) {
            return oidcUser.getIdToken().getTokenValue();
        }
        return null;
    }
}
