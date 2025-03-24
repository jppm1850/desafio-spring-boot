package com.spa.config;

import com.spa.model.entity.EstadoTarea;
import com.spa.model.entity.Usuario;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.spa.repository.EstadoTareaRepository;
import com.spa.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final EstadoTareaRepository estadoTareaRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        log.info("Inicializando datos...");

        // Primero verificamos si ya existen datos
        Mono<Boolean> estadosExisten = estadoTareaRepository.count().map(count -> count > 0);
        Mono<Boolean> usuariosExisten = usuarioRepository.count().map(count -> count > 0);

        // Si no existen estados, los creamos
        estadosExisten.flatMap(existen -> {
            if (!existen) {
                log.info("Creando estados de tarea...");
                return crearEstados();
            } else {
                log.info("Los estados de tarea ya existen, omitiendo creación");
                return Mono.empty();
            }
        }).subscribe();

        // Si no existen usuarios, los creamos
        usuariosExisten.flatMap(existen -> {
            if (!existen) {
                log.info("Creando usuarios...");
                return crearUsuarios();
            } else {
                log.info("Los usuarios ya existen, omitiendo creación");
                return Mono.empty();
            }
        }).subscribe();
    }

    private Mono<Void> crearEstados() {
        Flux<EstadoTarea> estados = Flux.just(
                EstadoTarea.builder().nombre("PENDIENTE").build(),
                EstadoTarea.builder().nombre("EN_PROGRESO").build(),
                EstadoTarea.builder().nombre("BLOQUEADA").build(),
                EstadoTarea.builder().nombre("COMPLETADA").build(),
                EstadoTarea.builder().nombre("CANCELADA").build()
        );

        return estadoTareaRepository.saveAll(estados)
                .doOnComplete(() -> log.info("Estados de tarea creados correctamente"))
                .then();
    }

    private Mono<Void> crearUsuarios() {
        Flux<Usuario> usuarios = Flux.just(
                Usuario.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .nombre("Administrador")
                        .email("admin@spa.com")
                        .build(),
                Usuario.builder()
                        .username("usuario1")
                        .password(passwordEncoder.encode("password123"))
                        .nombre("Juan Pérez")
                        .email("juan.perez@spa.com")
                        .build(),
                Usuario.builder()
                        .username("usuario2")
                        .password(passwordEncoder.encode("password123"))
                        .nombre("María Rodríguez")
                        .email("maria.rodriguez@spa.com")
                        .build()
        );

        return usuarioRepository.saveAll(usuarios)
                .doOnComplete(() -> log.info("Usuarios creados correctamente"))
                .then();
    }
}