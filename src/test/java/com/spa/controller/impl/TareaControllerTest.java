package com.spa.controller.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import com.spa.model.dto.EstadoTareaResponse;
import com.spa.model.dto.TareaRequest;
import com.spa.model.dto.TareaResponse;
import com.spa.model.dto.UsuarioResponse;
import com.spa.security.AuthenticationManager;
import com.spa.security.ReactiveUserDetailsServiceImpl;
import com.spa.config.SecurityConfig;
import com.spa.service.impl.JwtServiceImpl;
import com.spa.service.impl.TareaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;


import com.spa.exception.TareaNotFoundException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;

import java.util.Collections;

@WebFluxTest(controllers = TareaController.class)
@Import({JwtServiceImpl.class, SecurityConfig.class})
public class TareaControllerTest {

    @MockBean
    private TareaServiceImpl tareaService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private ReactiveUserDetailsServiceImpl reactiveUserDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private WebTestClient webTestClient;

    private TareaRequest tareaRequest;
    private TareaResponse tareaResponse;
    private EstadoTareaResponse estadoResponse;
    private UsuarioResponse usuarioResponse;
    private final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6InVzdWFyaW8xIiwiaWF0IjoxNjE2MTc4MjIwLCJleHAiOjE2MTYyNjQ2MjB9.wMmBUJLZlKlRpq8AXm5O7UhUEzZJ1wCQQtBKGxaU5Kk";

    @BeforeEach
    void setup() {
        // Setup test data
        estadoResponse = new EstadoTareaResponse();
        estadoResponse.setId(1L);
        estadoResponse.setNombre("PENDIENTE");

        usuarioResponse = new UsuarioResponse();
        usuarioResponse.setId(1L);
        usuarioResponse.setUsername("usuario1");
        usuarioResponse.setNombre("Usuario Uno");
        usuarioResponse.setEmail("usuario1@example.com");

        tareaResponse = new TareaResponse();
        tareaResponse.setId(1L);
        tareaResponse.setTitulo("Tarea de prueba");
        tareaResponse.setDescripcion("Descripción de prueba");
        tareaResponse.setFechaCreacion(LocalDateTime.parse("2025-03-22T12:00:00"));
        tareaResponse.setFechaVencimiento(LocalDate.parse("2025-03-29"));
        tareaResponse.setEstado(estadoResponse);
        tareaResponse.setUsuario(usuarioResponse);

        tareaRequest = new TareaRequest();
        tareaRequest.setTitulo("Tarea de prueba");
        tareaRequest.setDescripcion("Descripción de prueba");
        tareaRequest.setFechaVencimiento(LocalDate.parse("2025-03-29"));
        tareaRequest.setEstadoId(1L);

        // Configure JWT service mocks
        when(jwtService.validateToken(anyString())).thenReturn(true);
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(jwtService.extractUsername(anyString())).thenReturn("usuario1");

        // Configure UserDetails mock
        UserDetails userDetails = User.builder()
                .username("usuario1")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(reactiveUserDetailsService.findByUsername(anyString())).thenReturn(Mono.just(userDetails));

        // Configure Authentication Manager mock
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "usuario1", TOKEN, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(auth));

        // Configure WebTestClient with security
        webTestClient = webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf()); // Disable CSRF for testing
    }

    @Test
    @WithMockUser
    void listarTareas_ConTokenValido_DeberiaRetornarTareas() {
        // Given
        when(tareaService.obtenerTareasPorUsuario(anyLong()))
                .thenReturn(Flux.just(tareaResponse));

        // When, Then
        webTestClient.get()
                .uri("/api/tareas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TareaResponse.class)
                .hasSize(1)
                .contains(tareaResponse);
    }

    @Test
    void listarTareas_SinToken_DeberiaRetornarUnauthorized() {
        // When, Then
        webTestClient.get()
                .uri("/api/tareas")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    void obtenerTarea_ConTokenValidoYTareaExistente_DeberiaRetornarTarea() {
        // Given
        when(tareaService.obtenerTareaPorId(anyLong()))
                .thenReturn(Mono.just(tareaResponse));

        // When, Then
        webTestClient.get()
                .uri("/api/tareas/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TareaResponse.class)
                .isEqualTo(tareaResponse);
    }

    @Test
    @WithMockUser
    void obtenerTarea_ConTokenValidoYTareaInexistente_DeberiaRetornarNotFound() {
        // Given
        when(tareaService.obtenerTareaPorId(anyLong()))
                .thenReturn(Mono.error(new TareaNotFoundException(999L)));

        // When, Then
        webTestClient.get()
                .uri("/api/tareas/999")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser
    void crearTarea_ConTokenValido_DeberiaCrearYRetornarTarea() {
        // Given
        when(tareaService.crearTarea(any(TareaRequest.class), anyLong()))
                .thenReturn(Mono.just(tareaResponse));

        // When, Then
        webTestClient.post()
                .uri("/api/tareas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tareaRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TareaResponse.class)
                .isEqualTo(tareaResponse);
    }

    @Test
    @WithMockUser
    void actualizarTarea_ConTokenValidoYTareaExistente_DeberiaActualizarYRetornarTarea() {
        // Given
        when(tareaService.actualizarTarea(anyLong(), any(TareaRequest.class)))
                .thenReturn(Mono.just(tareaResponse));

        // When, Then
        webTestClient.put()
                .uri("/api/tareas/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tareaRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TareaResponse.class)
                .isEqualTo(tareaResponse);
    }

    @Test
    @WithMockUser
    void actualizarTarea_ConTokenValidoYTareaInexistente_DeberiaRetornarNotFound() {
        // Given
        when(tareaService.actualizarTarea(anyLong(), any(TareaRequest.class)))
                .thenReturn(Mono.error(new TareaNotFoundException(999L)));

        // When, Then
        webTestClient.put()
                .uri("/api/tareas/999")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tareaRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser
    void eliminarTarea_ConTokenValidoYTareaExistente_DeberiaEliminarTarea() {
        // Given
        when(tareaService.eliminarTarea(anyLong()))
                .thenReturn(Mono.empty());

        // When, Then
        webTestClient.delete()
                .uri("/api/tareas/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @WithMockUser
    void eliminarTarea_ConTokenValidoYTareaInexistente_DeberiaRetornarNotFound() {
        // Given
        when(tareaService.eliminarTarea(anyLong()))
                .thenReturn(Mono.error(new TareaNotFoundException(999L)));

        // When, Then
        webTestClient.delete()
                .uri("/api/tareas/999")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser
    void listarEstados_ConTokenValido_DeberiaRetornarEstados() {
        // Given
        EstadoTareaResponse estado1 = new EstadoTareaResponse();
        estado1.setId(1L);
        estado1.setNombre("PENDIENTE");

        EstadoTareaResponse estado2 = new EstadoTareaResponse();
        estado2.setId(2L);
        estado2.setNombre("EN_PROGRESO");

        when(tareaService.obtenerEstados())
                .thenReturn(Flux.fromIterable(Arrays.asList(estado1, estado2)));

        // When, Then
        webTestClient.get()
                .uri("/api/estados")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EstadoTareaResponse.class)
                .hasSize(2);
    }
}