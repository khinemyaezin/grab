package com.grab.service.sellerbff.internal.adapter.inbound.rest.controller;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.assembler.SellerUserProfileModelAssembler;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.SellerRegisterRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerCsrfResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcBrowserSession;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.util.SellerAuthCookieHelper;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerAuthCommandService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grab.seller-oidc.enabled", havingValue = "true")
public class SellerOidcAuthController {
    private final SellerAuthCommandService commands;
    private final SellerUserProfileModelAssembler profiles;
    private final SellerOidcBrowserSession sessions;
    private final SellerAuthCookieHelper cookies;

    @GetMapping("/csrf")
    public ResponseEntity<EntityModel<SellerCsrfResponse>> csrf(CsrfToken token) {
        SellerCsrfResponse value = new SellerCsrfResponse(token.getHeaderName(), token.getParameterName(), token.getToken());
        EntityModel<SellerCsrfResponse> model = EntityModel.of(value);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(model);
    }

    @PostMapping("/register")
    public ResponseEntity<EntityModel<SellerUserProfileResponse>> register(@Valid @RequestBody SellerRegisterRequest request) {
        SellerUserProfileResponse registered = commands.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profiles.toModel(registered));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        URI destination = sessions.logout(request, response);
        HttpHeaders legacy = cookies.clearTokenCookies();
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(destination).headers(legacy)
                .header("Referrer-Policy", "no-referrer")
                .cacheControl(CacheControl.noStore()).build();
    }
}
