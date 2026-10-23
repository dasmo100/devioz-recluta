package com.devioz.recluta.entrevistas.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado actual de la entrevista técnica o de recursos humanos")
public enum EstadoEntrevista {
    PROGRAMADA,
    REALIZADA,
    CANCELADA
}
