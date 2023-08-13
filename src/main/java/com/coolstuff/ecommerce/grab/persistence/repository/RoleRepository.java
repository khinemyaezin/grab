package com.coolstuff.ecommerce.grab.persistence.repository;

import com.coolstuff.ecommerce.grab.persistence.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    List<Role> findByNameIn(String[] names);
    Role findByName(String name);
}
