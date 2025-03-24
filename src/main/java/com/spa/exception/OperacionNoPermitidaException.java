package com.spa.exception;

/**
 * Excepción lanzada cuando una operación no es permitida
 */
public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
