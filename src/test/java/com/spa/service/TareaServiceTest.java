package com.spa.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.spa.model.dto.EstadoTareaResponse;
import com.spa.model.dto.TareaRequest;
import com.spa.model.dto.TareaResponse;
import com.spa.model.entity.EstadoTarea;
import com.spa.model.entity.Tarea;
import com.spa.model.entity.Usuario;
import com.spa.service.impl.TareaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.spa.exception.TareaNotFoundException;

import com.spa.repository.EstadoTareaRepository;
import com.spa.repository.TareaRepository;
import com.spa.repository.UsuarioRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EstadoTareaRepository estadoTareaRepository;

    @InjectMocks
    private TareaServiceImpl tareaService;

    private Usuario usuario;
    private EstadoTarea estado;
    private Tarea tarea;
    private TareaRequest tareaRequest;

    @BeforeEach
    void setup() {
        // Setup test data
        usuario = Usuario.builder()
                .id(1L)
                .username("usuario1")
                .password("password")
                .nombre("Usuario Uno")
                .email("usuario1@example.com")
                .build();

        estado = EstadoTarea.builder()
                .id(1L)
                .nombre("PENDIENTE")
                .build();

        tarea = Tarea.builder()
                .id(1L)
                .titulo("Tarea de prueba")
                .descripcion("Descripción de prueba")
                .fechaCreacion(LocalDateTime.now())
                .fechaVencimiento(LocalDate.now().plusDays(7))
                .estadoId(1L)
                .usuarioId(1L)
                .estado(estado)
                .usuario(usuario)
                .build();

        tareaRequest = new TareaRequest();
        tareaRequest.setTitulo("Tarea de prueba");
        tareaRequest.setDescripcion("Descripción de prueba");
        tareaRequest.setFechaVencimiento(LocalDate.now().plusDays(7));
        tareaRequest.setEstadoId(1L);
    }

    @Test
    void obtenerTareasPorUsuario_DeberiaRetornarTareasDelUsuario() {
        // Given
        when(tareaRepository.findByUsuarioIdOrderByFechaCreacionDesc(anyLong()))
                .thenReturn(Flux.just(tarea));
        when(usuarioRepository.findById(anyLong()))
                .thenReturn(Mono.just(usuario));
        when(estadoTareaRepository.findById(anyLong()))
                .thenReturn(Mono.just(estado));

        // When
        Flux<TareaResponse> result = tareaService.obtenerTareasPorUsuario(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(tareaResponse ->
                        tareaResponse.getId().equals(1L) &&
                                tareaResponse.getTitulo().equals("Tarea de prueba") &&
                                tareaResponse.getDescripcion().equals("Descripción de prueba")
                )
                .verifyComplete();
    }

    @Test
    void obtenerTareaPorId_CuandoExisteLaTarea_DeberiaRetornarLaTarea() {
        // Given
        when(tareaRepository.findById(anyLong()))
                .thenReturn(Mono.just(tarea));
        when(usuarioRepository.findById(anyLong()))
                .thenReturn(Mono.just(usuario));
        when(estadoTareaRepository.findById(anyLong()))
                .thenReturn(Mono.just(estado));

        // When
        Mono<TareaResponse> result = tareaService.obtenerTareaPorId(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(tareaResponse ->
                        tareaResponse.getId().equals(1L) &&
                                tareaResponse.getTitulo().equals("Tarea de prueba")
                )
                .verifyComplete();
    }

    @Test
    void obtenerTareaPorId_CuandoNoExisteLaTarea_DeberiLanzarExcepcion() {
        // Given
        when(tareaRepository.findById(anyLong()))
                .thenReturn(Mono.empty());

        // When
        Mono<TareaResponse> result = tareaService.obtenerTareaPorId(999L);

        // Then
        StepVerifier.create(result)
                .expectError(TareaNotFoundException.class)
                .verify();
    }

    @Test
    void crearTarea_DeberiaGuardarYRetornarLaTarea() {
        // Given
        when(usuarioRepository.findById(anyLong()))
                .thenReturn(Mono.just(usuario));
        when(estadoTareaRepository.findById(anyLong()))
                .thenReturn(Mono.just(estado));
        when(tareaRepository.save(any(Tarea.class)))
                .thenReturn(Mono.just(tarea));

        // When
        Mono<TareaResponse> result = tareaService.crearTarea(tareaRequest, 1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(tareaResponse ->
                        tareaResponse.getId().equals(1L) &&
                                tareaResponse.getTitulo().equals("Tarea de prueba")
                )
                .verifyComplete();
    }

    @Test
    void actualizarTarea_CuandoExisteLaTarea_DeberiaActualizarYRetornarLaTarea() {
        // Given
        Tarea tareaActualizada = Tarea.builder()
                .id(1L)
                .titulo("Tarea actualizada")
                .descripcion("Descripción actualizada")
                .fechaCreacion(tarea.getFechaCreacion())
                .fechaVencimiento(LocalDate.now().plusDays(14))
                .estadoId(2L)
                .usuarioId(1L)
                .estado(estado)
                .usuario(usuario)
                .build();

        TareaRequest requestActualizada = new TareaRequest();
        requestActualizada.setTitulo("Tarea actualizada");
        requestActualizada.setDescripcion("Descripción actualizada");
        requestActualizada.setFechaVencimiento(LocalDate.now().plusDays(14));
        requestActualizada.setEstadoId(2L);

        when(tareaRepository.findById(anyLong()))
                .thenReturn(Mono.just(tarea));
        when(estadoTareaRepository.findById(anyLong()))
                .thenReturn(Mono.just(estado));
        when(tareaRepository.save(any(Tarea.class)))
                .thenReturn(Mono.just(tareaActualizada));
        when(usuarioRepository.findById(anyLong()))
                .thenReturn(Mono.just(usuario));

        // When
        Mono<TareaResponse> result = tareaService.actualizarTarea(1L, requestActualizada);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(tareaResponse ->
                        tareaResponse.getId().equals(1L) &&
                                tareaResponse.getTitulo().equals("Tarea actualizada") &&
                                tareaResponse.getDescripcion().equals("Descripción actualizada")
                )
                .verifyComplete();
    }

    @Test
    void actualizarTarea_CuandoNoExisteLaTarea_DeberiLanzarExcepcion() {
        // Given
        when(tareaRepository.findById(anyLong()))
                .thenReturn(Mono.empty());

        // When
        Mono<TareaResponse> result = tareaService.actualizarTarea(999L, tareaRequest);

        // Then
        StepVerifier.create(result)
                .expectError(TareaNotFoundException.class)
                .verify();
    }

    @Test
    void eliminarTarea_CuandoExisteLaTarea_DeberiaEliminarLaTarea() {
        // Given
        when(tareaRepository.findById(anyLong()))
                .thenReturn(Mono.just(tarea));
        when(tareaRepository.deleteById(anyLong()))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = tareaService.eliminarTarea(1L);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        verify(tareaRepository).deleteById(1L);
    }

    @Test
    void eliminarTarea_CuandoNoExisteLaTarea_DeberiLanzarExcepcion() {
        // Given
        when(tareaRepository.findById(anyLong()))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = tareaService.eliminarTarea(999L);

        // Then
        StepVerifier.create(result)
                .expectError(TareaNotFoundException.class)
                .verify();
    }

    @Test
    void obtenerEstados_DeberiaRetornarTodosLosEstados() {
        // Given
        List<EstadoTarea> estados = Arrays.asList(
                EstadoTarea.builder().id(1L).nombre("PENDIENTE").build(),
                EstadoTarea.builder().id(2L).nombre("EN_PROGRESO").build(),
                EstadoTarea.builder().id(3L).nombre("COMPLETADA").build()
        );

        when(estadoTareaRepository.findAll())
                .thenReturn(Flux.fromIterable(estados));

        // When
        Flux<EstadoTareaResponse> result = tareaService.obtenerEstados();

        // Then
        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
    }

    }