package com.spa.security;


import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;

import com.spa.service.JwtService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationManager  implements ReactiveAuthenticationManager {

    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        return Mono.just(jwtService.validateToken(authToken))
                .filter(valid -> valid)
                .flatMap(valid -> {
                    String username = jwtService.extractUsername(authToken);
                    return userDetailsService.findByUsername(username);
                })
                .map(userDetails -> {
                    return new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(),
                            authToken,
                            userDetails.getAuthorities()
                    );
                });
    }
}
