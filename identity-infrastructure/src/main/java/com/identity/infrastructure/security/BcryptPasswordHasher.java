package com.identity.infrastructure.security;

import com.identity.domain.service.PasswordHasher;
import com.identity.domain.valueobject.HashedPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class BcryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public HashedPassword hash(String rawPassword) {
        return new HashedPassword(encoder.encode(rawPassword));
    }

    @Override
    public boolean verify(String rawPassword, HashedPassword hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword.hash());
    }
}
