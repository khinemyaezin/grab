package com.coolstuff.ecommerce.grab.configuration;

import com.coolstuff.ecommerce.grab.persistence.repository.TokenRepository;
import com.coolstuff.ecommerce.grab.utility.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final String[] skipUrls;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String tokenFromHeader = (authHeader != null && authHeader.startsWith(SecurityConstants.AUTH_HEADER_PREFIX))
                ? authHeader.substring(SecurityConstants.AUTH_HEADER_PREFIX.length())
                : null;

        String tokenFromCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(SecurityConstants.TOKEN_COOKIE_KEY))
                .findFirst().map(Cookie::getValue).orElse(null);

        String jwt = tokenFromHeader != null ? tokenFromHeader : tokenFromCookie;

        if (jwt == null ) {
            filterChain.doFilter(request, response);
            return;
        }

        String userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            boolean isTokenValid = tokenRepository.findByToken(jwt)
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);

            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                logger.info(userDetails.getAuthorities().toString());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {

        boolean shouldNotFilter = Arrays.stream(skipUrls).anyMatch(url ->
                request.getServletPath().contains(url.replaceAll("\\*\\*", "")));
        logger.info(request.getServletPath());
        logger.info("ShouldNotFilter {}", shouldNotFilter);
        return shouldNotFilter;
    }
}
