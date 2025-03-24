package com.spa.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.spa.model.dto.LoginRequest;
import com.spa.model.dto.LoginResponse;
import com.spa.service.AuthService;
import com.spa.service.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.spa.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        return usuarioRepository.findByUsername(request.getUsername())
                .flatMap(usuario -> {
                    if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
                        return Mono.error(new BadCredentialsException("Contraseña incorrecta"));
                    }

                    Map<String, Object> claims = new HashMap<>();
                    claims.put("userId", usuario.getId());

                    // Asegúrate de importar org.springframework.security.core.userdetails.User
                    org.springframework.security.core.userdetails.User userDetails =
                            new org.springframework.security.core.userdetails.User(
                                    usuario.getUsername(),
                                    usuario.getPassword(),
                                    Collections.emptyList());

                    String token = jwtService.generateToken(claims, userDetails);

                    // Usar el patrón fluido de la clase LoginResponse generada por OpenAPI
                    LoginResponse response = new LoginResponse()
                            .token(token)
                            .username(usuario.getUsername())
                            .userId(usuario.getId());

                    return Mono.just(response);
                })
                .switchIfEmpty(Mono.error(new BadCredentialsException("Usuario no encontrado")));
    }
}