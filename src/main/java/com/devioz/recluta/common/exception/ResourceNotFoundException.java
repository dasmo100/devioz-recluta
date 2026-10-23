package com.devioz.recluta.common.exception;

/**
 * Excepción para recursos no encontrados en el paquete common.exception.
 * Hereda de {@link com.devioz.recluta.exception.ResourceNotFoundException} para mantener
 * total compatibilidad con el manejador global de excepciones.
 */
public class ResourceNotFoundException extends com.devioz.recluta.exception.ResourceNotFoundException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName, fieldName, fieldValue);
    }
}
