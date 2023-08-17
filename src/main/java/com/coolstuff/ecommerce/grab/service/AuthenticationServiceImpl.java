package com.coolstuff.ecommerce.grab.service;

import com.coolstuff.ecommerce.grab.constant.TokenType;
import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationRequest;
import com.coolstuff.ecommerce.grab.dto.auth.AuthenticationResponse;
import com.coolstuff.ecommerce.grab.persistence.entity.TokenEntity;
import com.coolstuff.ecommerce.grab.persistence.entity.UserEntity;
import com.coolstuff.ecommerce.grab.persistence.repository.TokenRepository;
import com.coolstuff.ecommerce.grab.persistence.repository.UserRepository;
import com.coolstuff.ecommerce.grab.utility.JwtService;
import com.coolstuff.ecommerce.grab.utility.UserUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserUtility userUtility;
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserName(),
                            request.getPassword()
                    )
            );
            UserEntity user = repository.findByUserNameOrEmail(request.getUserName()).orElseThrow();
            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);

            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Transactional
    private void revokeAllUserTokens(UserEntity user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
    @Transactional
    private void saveUserToken(UserEntity user, String jwtToken) {
        var token = TokenEntity.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

}
