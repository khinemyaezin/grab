package com.coolstuff.ecommerce.grab.api.auth;

import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationRequest;
import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/public/auth")
public interface AuthenticationController {
    @PostMapping("/login")
    ResponseEntity<AuthenticationResponse> authenticate( HttpServletResponse httpServletResponse,@RequestBody AuthenticationRequest request);
}
