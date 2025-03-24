package com.spa.exception;

import com.spa.model.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;



import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TareaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleTareaNotFoundException(TareaNotFoundException ex, ServerWebExchange exchange) {
        log.error("Tarea no encontrada: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, ServerWebExchange exchange) {
        log.error("Credenciales inválidas: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ErrorResponse> handleAuthenticationException(AuthenticationException ex, ServerWebExchange exchange) {
        log.error("Error de autenticación: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Error de autenticación");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, ServerWebExchange exchange) {
        log.error("Acceso denegado: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("Error de validación: {}", errorMessage);
        return buildErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Error de validación: " + errorMessage);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(ResponseStatusException ex, ServerWebExchange exchange) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        log.error("Error de respuesta: {} - {}", status, ex.getMessage());
        return buildErrorResponse(exchange, status, ex.getReason())
                .map(errorResponse -> ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleInvalidDataException(InvalidDataException ex, ServerWebExchange exchange) {
        log.error("Datos inválidos: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleResourceConflictException(ResourceConflictException ex, ServerWebExchange exchange) {
        log.error("Conflicto de recursos: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ErrorResponse> handleOperacionNoPermitidaException(OperacionNoPermitidaException ex, ServerWebExchange exchange) {
        log.error("Operación no permitida: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("Error interno del servidor", ex);
        return buildErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private Mono<ErrorResponse> buildErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        String path = exchange.getRequest().getURI().getPath();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(message);
        errorResponse.setPath(path);

        return Mono.just(errorResponse);
    }
}
