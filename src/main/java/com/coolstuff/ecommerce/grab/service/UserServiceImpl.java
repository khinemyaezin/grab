package com.coolstuff.ecommerce.grab.service;

import com.coolstuff.ecommerce.grab.persistence.entity.UserEntity;
import com.coolstuff.ecommerce.grab.persistence.repository.UserRepository;
import com.coolstuff.ecommerce.grab.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {
        UserEntity user = this.userRepository.findByUserNameOrEmail(value).orElseThrow();
        return user;
    }
}
