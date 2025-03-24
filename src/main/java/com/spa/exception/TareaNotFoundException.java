package com.spa.exception;

/**
 * Excepción lanzada cuando una tarea no se encuentra
 */
public class TareaNotFoundException extends RuntimeException {
    public TareaNotFoundException(Long id) {
        super("No se encontró la tarea con ID: " + id);
    }
}
