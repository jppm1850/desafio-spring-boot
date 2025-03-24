package com.spa.repository;

import com.spa.model.entity.Tarea;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface TareaRepository extends R2dbcRepository<Tarea, Long> {

    Flux<Tarea> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    @Query("SELECT t.* FROM tareas t " +
            "JOIN usuarios u ON t.usuario_id = u.id " +
            "WHERE u.username = :username " +
            "ORDER BY t.fecha_creacion DESC")
    Flux<Tarea> findByUsernameOrderByFechaCreacionDesc(String username);
}
