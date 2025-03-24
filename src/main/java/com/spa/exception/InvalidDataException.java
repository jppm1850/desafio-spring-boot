package com.spa.exception;

/**
 * Excepción lanzada cuando hay un problema con la validación de datos
 */
public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }
}
