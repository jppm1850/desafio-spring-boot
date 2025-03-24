package com.spa.exception;

/**
 * Excepción lanzada cuando hay un conflicto de recursos
 */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
