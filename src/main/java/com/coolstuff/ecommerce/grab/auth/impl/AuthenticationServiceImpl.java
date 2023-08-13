package com.coolstuff.ecommerce.grab.auth.impl;

import com.coolstuff.ecommerce.grab.auth.AuthenticationService;
import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationResponse;
import com.coolstuff.ecommerce.grab.dto.auth.RegisterRequest;
import com.coolstuff.ecommerce.grab.persistence.entity.Role;
import com.coolstuff.ecommerce.grab.persistence.entity.User;
import com.coolstuff.ecommerce.grab.persistence.repository.RoleRepository;
import com.coolstuff.ecommerce.grab.persistence.repository.UserRepository;
import com.coolstuff.ecommerce.grab.utility.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        User.UserBuilder user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .password(passwordEncoder.encode(request.getPassword()));

        List<Role> roles = roleRepository.findByNameIn(new String[]{ request.getRoleName() });
        user.roles(roles);

        var savedUser = userRepository.save( user.build() );
        var jwtToken = jwtService.generateToken(savedUser);
        var refreshToken = jwtService.generateRefreshToken(savedUser);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}
