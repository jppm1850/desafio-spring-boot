package com.spa.service;

import com.spa.model.dto.EstadoTareaResponse;
import com.spa.model.dto.TareaRequest;
import com.spa.model.dto.TareaResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface TareaService {

    public Flux<TareaResponse> obtenerTareasPorUsuario(Long usuarioId) ;

    public Mono<TareaResponse> obtenerTareaPorId(Long id) ;

    public Mono<TareaResponse> crearTarea(TareaRequest request, Long usuarioId);

    public Mono<TareaResponse> actualizarTarea(Long id, TareaRequest request);

    public Mono<Void> eliminarTarea(Long id);

    public Flux<EstadoTareaResponse> obtenerEstados();


}
