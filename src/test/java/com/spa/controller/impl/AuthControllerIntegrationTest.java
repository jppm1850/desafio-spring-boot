package com.spa.controller.impl;


import com.spa.model.dto.LoginRequest;
import com.spa.model.dto.LoginResponse;
import com.spa.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @MockBean
    private AuthService authService;

    @Autowired
    private WebTestClient webTestClient;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    public void setup() {
        // Configurar solicitud de login
        loginRequest = new LoginRequest();
        loginRequest.setUsername("usuario1");
        loginRequest.setPassword("password123");

        // Configurar respuesta de login
        loginResponse = new LoginResponse();
        loginResponse.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        loginResponse.setUsername("usuario1");
        loginResponse.setUserId(1L);
    }

    @Test
    public void login_ConCredencialesValidas_DeberiaRetornarTokenYDatos() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Mono.just(loginResponse));

        // When, Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .isEqualTo(loginResponse);
    }

    @Test
    public void login_ConCredencialesInvalidas_DeberiaRetornarUnauthorized() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Mono.error(new BadCredentialsException("Credenciales inválidas")));

        // When, Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void login_ConUsuarioNoEncontrado_DeberiaRetornarUnauthorized() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Mono.error(new BadCredentialsException("Usuario no encontrado")));

        // When, Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void login_ConRequestInvalido_DeberiaRetornarBadRequest() {
        // Given
        LoginRequest invalidRequest = new LoginRequest();

        // When, Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void login_ConErrorInterno_DeberiaRetornarInternalServerError() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Error interno")));

        // When, Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    public void login_ConCredencialesValidas_DeberiaIncluirDatosCorrectos() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Mono.just(loginResponse));

        // When, Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo(loginResponse.getToken())
                .jsonPath("$.username").isEqualTo(loginResponse.getUsername())
                .jsonPath("$.userId").isEqualTo(loginResponse.getUserId());
    }
}