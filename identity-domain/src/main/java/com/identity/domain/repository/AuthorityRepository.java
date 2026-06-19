package com.identity.domain.repository;

public interface AuthorityRepository {
    boolean existsByCode(String code);
}
