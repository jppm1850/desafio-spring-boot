package com.spa.repository;

import com.spa.model.entity.EstadoTarea;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface EstadoTareaRepository  extends R2dbcRepository<EstadoTarea, Long> {

    Mono<EstadoTarea> findByNombre(String nombre);
}
