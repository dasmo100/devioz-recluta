package com.devioz.recluta.common.exception;

/**
 * Excepción para violaciones de reglas de negocio en el paquete common.exception.
 * Hereda de {@link com.devioz.recluta.exception.BusinessRuleException} para mantener
 * total compatibilidad con el manejador global de excepciones.
 */
public class BusinessRuleException extends com.devioz.recluta.exception.BusinessRuleException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
