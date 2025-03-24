package com.spa.controller.impl;

import java.time.LocalDateTime;

import com.spa.api.AutenticacionApi;
import com.spa.model.dto.ErrorResponse;
import com.spa.model.dto.LoginRequest;
import com.spa.model.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.spa.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AutenticacionApi {

    private final AuthService authService;

    @Override
    public Mono<ResponseEntity<LoginResponse>> login(Mono<LoginRequest> loginRequest, ServerWebExchange exchange) {
        log.info("Procesando solicitud de login");

        return loginRequest.flatMap(request ->
                authService.login(request)
                        .map(response -> ResponseEntity.ok().body(response))
                        .onErrorResume(error -> {
                            log.error("Error de autenticación: {}", error.getMessage());

                            if (error instanceof BadCredentialsException) {
                                ErrorResponse errorResponse = buildErrorResponse(
                                        HttpStatus.UNAUTHORIZED,
                                        "Unauthorized",
                                        error.getMessage(),
                                        "/api/auth/login"
                                );

                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                exchange.getResponse().getHeaders().add("Content-Type", "application/json");

                                // Devolvemos null para el LoginResponse porque ya hemos configurado la respuesta de error
                                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
                            } else {
                                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                            }
                        })
        );
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String error, String message, String path) {
        ErrorResponse response = new ErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(status.value());
        response.setError(error);
        response.setMessage(message);
        response.setPath(path);
        return response;
    }
}
