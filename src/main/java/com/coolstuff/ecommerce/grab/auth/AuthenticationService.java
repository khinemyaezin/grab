package com.coolstuff.ecommerce.grab.auth;

import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationResponse;
import com.coolstuff.ecommerce.grab.dto.auth.RegisterRequest;

public interface AuthenticationService {
    public AuthenticationResponse register(RegisterRequest request);
}
