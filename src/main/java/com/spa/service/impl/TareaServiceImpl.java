package com.spa.service.impl;

import java.time.LocalDateTime;


import com.spa.model.dto.EstadoTareaResponse;
import com.spa.model.dto.TareaRequest;
import com.spa.model.dto.TareaResponse;
import com.spa.model.dto.UsuarioResponse;
import com.spa.model.entity.EstadoTarea;
import com.spa.model.entity.Tarea;
import com.spa.model.entity.Usuario;
import com.spa.exception.TareaNotFoundException;
import com.spa.service.TareaService;
import org.springframework.stereotype.Service;

import com.spa.repository.EstadoTareaRepository;
import com.spa.repository.TareaRepository;
import com.spa.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TareaServiceImpl implements TareaService {


    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoTareaRepository estadoTareaRepository;

    public Flux<TareaResponse> obtenerTareasPorUsuario(Long usuarioId) {
        log.info("Obteniendo tareas para el usuario con ID: {}", usuarioId);
        return tareaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .flatMap(this::enrichTareaWithReferences)
                .map(this::mapToTareaResponse);
    }

    public Mono<TareaResponse> obtenerTareaPorId(Long id) {
        log.info("Obteniendo tarea con ID: {}", id);
        return tareaRepository.findById(id)
                .switchIfEmpty(Mono.error(new TareaNotFoundException(id)))
                .flatMap(this::enrichTareaWithReferences)
                .map(this::mapToTareaResponse);
    }

    public Mono<TareaResponse> crearTarea(TareaRequest request, Long usuarioId) {
        log.info("Creando nueva tarea para el usuario con ID: {}", usuarioId);
        return Mono.zip(
                        usuarioRepository.findById(usuarioId),
                        estadoTareaRepository.findById(request.getEstadoId())
                )
                .flatMap(tuple -> {
                    Usuario usuario = tuple.getT1();
                    EstadoTarea estado = tuple.getT2();

                    log.debug("Creando tarea con título: {}, estado: {}", request.getTitulo(), estado.getNombre());


                    Tarea tarea = Tarea.builder()
                            .titulo(request.getTitulo())
                            .descripcion(request.getDescripcion())
                            .fechaCreacion(LocalDateTime.now())
                            .fechaVencimiento(request.getFechaVencimiento())
                            .estadoId(estado.getId())
                            .usuarioId(usuario.getId())
                            .build();

                    return tareaRepository.save(tarea);
                })
                .flatMap(this::enrichTareaWithReferences)
                .map(this::mapToTareaResponse);
    }

    public Mono<TareaResponse> actualizarTarea(Long id, TareaRequest request) {
        log.info("Actualizando tarea con ID: {}", id);
        return tareaRepository.findById(id)
                .switchIfEmpty(Mono.error(new TareaNotFoundException(id)))
                .flatMap(tarea -> {
                    tarea.setTitulo(request.getTitulo());
                    tarea.setDescripcion(request.getDescripcion());

                        tarea.setFechaVencimiento(request.getFechaVencimiento());


                    log.debug("Actualizando estado de tarea a ID: {}", request.getEstadoId());

                    return estadoTareaRepository.findById(request.getEstadoId())
                            .flatMap(estadoTarea -> {
                                tarea.setEstadoId(estadoTarea.getId());
                                return tareaRepository.save(tarea);
                            });
                })
                .flatMap(this::enrichTareaWithReferences)
                .map(this::mapToTareaResponse);
    }

    public Mono<Void> eliminarTarea(Long id) {
        log.info("Eliminando tarea con ID: {}", id);
        return tareaRepository.findById(id)
                .switchIfEmpty(Mono.error(new TareaNotFoundException(id)))
                .flatMap(tarea -> tareaRepository.deleteById(tarea.getId()));
    }

    public Flux<EstadoTareaResponse> obtenerEstados() {
        log.info("Obteniendo todos los estados de tarea");
        return estadoTareaRepository.findAll()
                .map(estado -> {
                    EstadoTareaResponse response = new EstadoTareaResponse();
                    response.setId(estado.getId());
                    response.setNombre(estado.getNombre());
                    return response;
                });
    }

    private Mono<Tarea> enrichTareaWithReferences(Tarea tarea) {
        log.debug("Enriqueciendo tarea con ID: {} con referencias", tarea.getId());
        return Mono.zip(
                        usuarioRepository.findById(tarea.getUsuarioId()),
                        estadoTareaRepository.findById(tarea.getEstadoId())
                )
                .map(tuple -> {
                    tarea.setUsuario(tuple.getT1());
                    tarea.setEstado(tuple.getT2());
                    return tarea;
                });
    }

    private TareaResponse mapToTareaResponse(Tarea tarea) {
        // Crear el objeto estado
        EstadoTareaResponse estadoResponse = new EstadoTareaResponse();
        estadoResponse.setId(tarea.getEstado().getId());
        estadoResponse.setNombre(tarea.getEstado().getNombre());

        // Crear el objeto usuario
        UsuarioResponse usuarioResponse = new UsuarioResponse();
        usuarioResponse.setId(tarea.getUsuario().getId());
        usuarioResponse.setUsername(tarea.getUsuario().getUsername());
        usuarioResponse.setNombre(tarea.getUsuario().getNombre());
        usuarioResponse.setEmail(tarea.getUsuario().getEmail());

        // Crear la respuesta de tarea
        TareaResponse response = new TareaResponse();
        response.setId(tarea.getId());
        response.setTitulo(tarea.getTitulo());
        response.setDescripcion(tarea.getDescripcion());


            response.setFechaCreacion(tarea.getFechaCreacion());


        // Establecer la fecha de vencimiento
        response.setFechaVencimiento(tarea.getFechaVencimiento());

        // Establecer objetos anidados
        response.setEstado(estadoResponse);
        response.setUsuario(usuarioResponse);

        return response;
    }

}
