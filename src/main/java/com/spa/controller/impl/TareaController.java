package com.spa.controller.impl;

import com.spa.api.TareasApi;
import com.spa.model.dto.EstadoTareaResponse;
import com.spa.model.dto.TareaRequest;
import com.spa.model.dto.TareaResponse;
import com.spa.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;



import com.spa.service.TareaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TareaController implements TareasApi {

    private final TareaService tareaService;
    private final JwtService jwtService;

    // Método auxiliar para extraer userId del token en el encabezado de autorización
    private Mono<Long> extractUserIdFromToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Quitar "Bearer " del inicio
            Long userId = jwtService.extractUserId(token);

            if (userId != null) {
                return Mono.just(userId);
            }
        }

        return Mono.empty(); // Sin userId válido
    }

    @Override
    public Mono<ResponseEntity<TareaResponse>> actualizarTarea(Long id, Mono<TareaRequest> tareaRequest, ServerWebExchange exchange) {
        log.info("Actualizando tarea con ID: {}", id);

        return extractUserIdFromToken(exchange)
                .flatMap(userId -> tareaRequest.flatMap(request ->
                        tareaService.actualizarTarea(id, request)
                                .map(tarea -> ResponseEntity.ok().body(tarea))
                ))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)));
    }

    @Override
    public Mono<ResponseEntity<TareaResponse>> crearTarea(Mono<TareaRequest> tareaRequest, ServerWebExchange exchange) {
        log.info("Creando nueva tarea");

        return extractUserIdFromToken(exchange)
                .flatMap(userId -> tareaRequest.flatMap(request ->
                        tareaService.crearTarea(request, userId)
                                .map(tarea -> ResponseEntity.status(HttpStatus.CREATED).body(tarea))
                ))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)));
    }

    @Override
    public Mono<ResponseEntity<Void>> eliminarTarea(Long id, ServerWebExchange exchange) {
        log.info("Eliminando tarea con ID: {}", id);

        return extractUserIdFromToken(exchange)
                .flatMap(userId ->
                        tareaService.eliminarTarea(id)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<EstadoTareaResponse>>> listarEstados(ServerWebExchange exchange) {
        log.info("Listando estados de tarea");

        return extractUserIdFromToken(exchange)
                .map(userId -> ResponseEntity.ok().body(tareaService.obtenerEstados()))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Flux.empty())));
    }

    @Override
    public Mono<ResponseEntity<Flux<TareaResponse>>> listarTareas(ServerWebExchange exchange) {
        log.info("Listando tareas");

        return extractUserIdFromToken(exchange)
                .map(userId -> ResponseEntity.ok().body(tareaService.obtenerTareasPorUsuario(userId)))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Flux.empty())));
    }

    @Override
    public Mono<ResponseEntity<TareaResponse>> obtenerTarea(Long id, ServerWebExchange exchange) {
        log.info("Obteniendo tarea con ID: {}", id);

        return extractUserIdFromToken(exchange)
                .flatMap(userId ->
                        tareaService.obtenerTareaPorId(id)
                                .map(tarea -> ResponseEntity.ok().body(tarea))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)));
    }
}