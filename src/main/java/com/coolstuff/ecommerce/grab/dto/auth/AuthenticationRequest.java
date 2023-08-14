package com.coolstuff.ecommerce.grab.dto.auth;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String userName;
    private String password;
}
