package com.coolstuff.ecommerce.grab;

import com.coolstuff.ecommerce.grab.persistence.entity.PrivilegeEntity;
import com.coolstuff.ecommerce.grab.persistence.entity.RoleEntity;
import com.coolstuff.ecommerce.grab.persistence.entity.UserEntity;
import com.coolstuff.ecommerce.grab.persistence.repository.PrivilegeRepository;
import com.coolstuff.ecommerce.grab.persistence.repository.RoleRepository;
import com.coolstuff.ecommerce.grab.persistence.repository.UserRepository;
import com.coolstuff.ecommerce.grab.utility.UserUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserUtility userUtility;

    // API

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        // == create initial privileges
        final PrivilegeEntity readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        final PrivilegeEntity writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        final PrivilegeEntity passwordPrivilege = createPrivilegeIfNotFound("CHANGE_PASSWORD_PRIVILEGE");

        // == create initial roles
        final List<PrivilegeEntity> adminPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, writePrivilege, passwordPrivilege));
        final List<PrivilegeEntity> userPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, passwordPrivilege));
        final RoleEntity adminRole = createRoleIfNotFound("ADMIN", adminPrivileges);

        // == create initial user
        createUserIfNotFound("test@test.com", "Test", "Test", "test", new ArrayList<>(Arrays.asList(adminRole)));

        alreadySetup = true;
    }

    @Transactional
    public PrivilegeEntity createPrivilegeIfNotFound(final String name) {
        PrivilegeEntity privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = PrivilegeEntity.builder().name(name).build();
            privilege = privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    public RoleEntity createRoleIfNotFound(final String name, final List<PrivilegeEntity> privileges) {
        RoleEntity role = roleRepository.findByName(name);
        if (role == null) {
            role = RoleEntity.builder().name(name).build();
        }
        role.setPrivileges(privileges);
        role = roleRepository.save(role);
        return role;
    }

    @Transactional
    public UserEntity createUserIfNotFound(final String email, final String firstName, final String lastName, final String password, final List<RoleEntity> roles) {
        var user = userRepository.findByUserNameOrEmail(email).orElse(null);
        if (user == null) {
            user = new UserEntity();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setUserName( this.userUtility.generateUsername( user ));
            user.setRoles(roles);
        }
        user.setRoles(roles);
        user = userRepository.save(user);
        return user;
    }
}