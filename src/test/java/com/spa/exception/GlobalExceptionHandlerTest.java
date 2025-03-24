package com.spa.exception;


import com.spa.model.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServerWebExchange exchange;

    @Mock
    private WebExchangeBindException webExchangeBindException;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/tareas/1"));
    }

    @Test
    void handleTareaNotFoundException_DeberiaRetornarNotFound() {
        // Given
        TareaNotFoundException exception = new TareaNotFoundException(1L);

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleTareaNotFoundException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.NOT_FOUND.value() &&
                                errorResponse.getError().equals(HttpStatus.NOT_FOUND.getReasonPhrase()) &&
                                errorResponse.getMessage().contains("No se encontró la tarea con ID: 1") &&
                                errorResponse.getPath().equals("/api/tareas/1")
                )
                .verifyComplete();
    }

    @Test
    void handleBadCredentialsException_DeberiaRetornarUnauthorized() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Credenciales inválidas");

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleBadCredentialsException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.UNAUTHORIZED.value() &&
                                errorResponse.getError().equals(HttpStatus.UNAUTHORIZED.getReasonPhrase()) &&
                                errorResponse.getMessage().equals("Credenciales inválidas")
                )
                .verifyComplete();
    }

    @Test
    void handleAuthenticationException_DeberiaRetornarUnauthorized() {
        // Given
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn("Error de autenticación");

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleAuthenticationException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.UNAUTHORIZED.value() &&
                                errorResponse.getError().equals(HttpStatus.UNAUTHORIZED.getReasonPhrase()) &&
                                errorResponse.getMessage().equals("Error de autenticación")
                )
                .verifyComplete();
    }

    @Test
    void handleAccessDeniedException_DeberiaRetornarForbidden() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("No tiene permisos para realizar esta operación");

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleAccessDeniedException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.FORBIDDEN.value() &&
                                errorResponse.getError().equals(HttpStatus.FORBIDDEN.getReasonPhrase()) &&
                                errorResponse.getMessage().equals("No tiene permisos para realizar esta operación")
                )
                .verifyComplete();
    }

    @Test
    void handleValidationException_DeberiaRetornarBadRequest() {
        // Given
        WebExchangeBindException exception = mock(WebExchangeBindException.class);

        // Configurar el mock para que devuelva errores de campo simulados
        org.springframework.validation.FieldError fieldError1 = mock(org.springframework.validation.FieldError.class);
        when(fieldError1.getField()).thenReturn("titulo");
        when(fieldError1.getDefaultMessage()).thenReturn("El título es obligatorio");

        org.springframework.validation.FieldError fieldError2 = mock(org.springframework.validation.FieldError.class);
        when(fieldError2.getField()).thenReturn("fechaVencimiento");
        when(fieldError2.getDefaultMessage()).thenReturn("Formato de fecha inválido");

        // Configurar el mock de BindingResult
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Configurar el mock de WebExchangeBindException para que use el BindingResult
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleValidationException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.BAD_REQUEST.value() &&
                                errorResponse.getError().equals(HttpStatus.BAD_REQUEST.getReasonPhrase()) &&
                                errorResponse.getMessage().contains("Error de validación") &&
                                errorResponse.getMessage().contains("titulo: El título es obligatorio") &&
                                errorResponse.getMessage().contains("fechaVencimiento: Formato de fecha inválido")
                )
                .verifyComplete();
    }

    @Test
    void handleResponseStatusException_DeberiaRetornarStatusDeExcepcion() {
        // Given
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error en gateway remoto");

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleResponseStatusException(exception, exchange)
                .map(responseEntity -> responseEntity.getBody());

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.BAD_GATEWAY.value() &&
                                errorResponse.getError().equals(HttpStatus.BAD_GATEWAY.getReasonPhrase()) &&
                                errorResponse.getMessage().equals("Error en gateway remoto")
                )
                .verifyComplete();
    }

    @Test
    void handleGenericException_DeberiaRetornarInternalServerError() {
        // Given
        Exception exception = new Exception("Error interno inesperado");

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleGenericException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value() &&
                                errorResponse.getError().equals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()) &&
                                errorResponse.getMessage().equals("Error interno del servidor")
                )
                .verifyComplete();
    }

    @Test
    void handleInvalidDataException_DeberiaRetornarBadRequest() {
        // Given
        InvalidDataException exception = new InvalidDataException("Datos inválidos en el campo nombre");

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleInvalidDataException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.BAD_REQUEST.value() &&
                                errorResponse.getError().equals(HttpStatus.BAD_REQUEST.getReasonPhrase()) &&
                                errorResponse.getMessage().equals("Datos inválidos en el campo nombre")
                )
                .verifyComplete();
    }

    @Test
    void handleResourceConflictException_DeberiaRetornarConflict() {
        // Given
        ResourceConflictException exception = new ResourceConflictException("El usuario ya existe en el sistema");

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleResourceConflictException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.CONFLICT.value() &&
                                errorResponse.getError().equals(HttpStatus.CONFLICT.getReasonPhrase()) &&
                                errorResponse.getMessage().equals("El usuario ya existe en el sistema")
                )
                .verifyComplete();
    }

    @Test
    void handleOperacionNoPermitidaException_DeberiaRetornarForbidden() {
        // Given
        OperacionNoPermitidaException exception = new OperacionNoPermitidaException("No puede eliminar esta tarea");

        // When
        Mono<ErrorResponse> result = exceptionHandler.handleOperacionNoPermitidaException(exception, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(errorResponse ->
                        errorResponse.getStatus() == HttpStatus.FORBIDDEN.value() &&
                                errorResponse.getError().equals(HttpStatus.FORBIDDEN.getReasonPhrase()) &&
                                errorResponse.getMessage().equals("No puede eliminar esta tarea")
                )
                .verifyComplete();
    }
}
