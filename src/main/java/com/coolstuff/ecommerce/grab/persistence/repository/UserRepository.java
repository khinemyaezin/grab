package com.coolstuff.ecommerce.grab.persistence.repository;

import com.coolstuff.ecommerce.grab.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    @Query("SELECT u FROM UserEntity u WHERE u.email = :value OR u.userName = :value")
    Optional<UserEntity> findByUserNameOrEmail(String value);

}
