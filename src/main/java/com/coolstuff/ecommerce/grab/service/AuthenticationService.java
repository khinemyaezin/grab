package com.coolstuff.ecommerce.grab.service;

import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationRequest;
import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
