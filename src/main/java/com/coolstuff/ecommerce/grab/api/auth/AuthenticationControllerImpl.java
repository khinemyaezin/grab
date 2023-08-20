package com.coolstuff.ecommerce.grab.api.auth;

import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationRequest;
import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationResponse;
import com.coolstuff.ecommerce.grab.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.coolstuff.ecommerce.grab.configuration.SecurityConstants.TOKEN_COOKIE_KEY;

@RestController
@RequiredArgsConstructor
public class AuthenticationControllerImpl implements AuthenticationController {
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<AuthenticationResponse> authenticate(HttpServletResponse httpServletResponse, AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        Cookie cookie = new Cookie(TOKEN_COOKIE_KEY, response.getAccessToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Use secure if serving over HTTPS
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
        return ResponseEntity.ok( response );
    }
}
