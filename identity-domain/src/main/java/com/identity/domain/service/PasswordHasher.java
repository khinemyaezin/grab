package com.identity.domain.service;

import com.identity.domain.valueobject.HashedPassword;

public interface PasswordHasher {
    HashedPassword hash(String rawPassword);
    boolean verify(String rawPassword, HashedPassword hashedPassword);
}
