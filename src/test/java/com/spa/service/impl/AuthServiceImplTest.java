package com.spa.service.impl;


import com.spa.model.dto.LoginRequest;
import com.spa.model.dto.LoginResponse;
import com.spa.model.entity.Usuario;
import com.spa.repository.UsuarioRepository;
import com.spa.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuario;
    private LoginRequest loginRequest;
    private String token;

    @BeforeEach
    void setUp() {
        // Configurar usuario
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("usuario1");
        usuario.setPassword("hashedPassword");
        usuario.setNombre("Usuario Uno");
        usuario.setEmail("usuario1@example.com");

        // Configurar solicitud de login
        loginRequest = new LoginRequest();
        loginRequest.setUsername("usuario1");
        loginRequest.setPassword("password123");

        // Token JWT simulado
        token = "jwt.token.example";
    }

    @Test
    void login_ConCredencialesValidas_DeberiaRetornarLoginResponse() {
        // Given
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Mono.just(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyMap(), any())).thenReturn(token);

        // When
        Mono<LoginResponse> result = authService.login(loginRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getToken().equals(token) &&
                                response.getUsername().equals("usuario1") &&
                                response.getUserId().equals(1L)
                )
                .verifyComplete();
    }

    @Test
    void login_ConPasswordIncorrecto_DeberiaLanzarBadCredentialsException() {
        // Given
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Mono.just(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When
        Mono<LoginResponse> result = authService.login(loginRequest);

        // Then
        StepVerifier.create(result)
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void login_ConUsuarioNoEncontrado_DeberiaLanzarBadCredentialsException() {
        // Given
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        // When
        Mono<LoginResponse> result = authService.login(loginRequest);

        // Then
        StepVerifier.create(result)
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void login_DebeAgregarUserIdClaims() {
        // Given
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Mono.just(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Capturar los claims para verificar que contienen el userId
        when(jwtService.generateToken(anyMap(), any())).thenAnswer(invocation -> {
            Map<String, Object> claims = invocation.getArgument(0);
            // Verificar que claims contiene userId=1L
            if (claims.containsKey("userId") && claims.get("userId").equals(1L)) {
                return token;
            } else {
                return "invalid_token";
            }
        });

        // When
        Mono<LoginResponse> result = authService.login(loginRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getToken().equals(token))
                .verifyComplete();
    }

    @Test
    void login_DebeUtilizarDatosCorrectosParaConstructorDeUser() {
        // Given
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Mono.just(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Verificar que se crea el User con los datos correctos
        when(jwtService.generateToken(anyMap(), any())).thenAnswer(invocation -> {
            org.springframework.security.core.userdetails.User userDetails = invocation.getArgument(1);
            // Verificar que userDetails tiene el username correcto
            if (userDetails.getUsername().equals("usuario1") &&
                    userDetails.getPassword().equals("hashedPassword")) {
                return token;
            } else {
                return "invalid_token";
            }
        });

        // When
        Mono<LoginResponse> result = authService.login(loginRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getToken().equals(token))
                .verifyComplete();
    }
}