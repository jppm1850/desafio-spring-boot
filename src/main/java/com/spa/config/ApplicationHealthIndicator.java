package com.spa.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ApplicationHealthIndicator implements ReactiveHealthIndicator {

    @Override
    public Mono<Health> health() {
        return checkApplication()
                .map(available -> available
                        ? Health.up()
                        .withDetail("description", "Servicio de gestión de tareas")
                        .withDetail("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .withDetail("version", "1.0.0")
                        .build()
                        : Health.down()
                        .withDetail("error", "El servicio no está disponible")
                        .withDetail("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build());
    }

    private Mono<Boolean> checkApplication() {

        return Mono.just(true);
    }
}
