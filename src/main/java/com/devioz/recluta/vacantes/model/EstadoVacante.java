package com.devioz.recluta.vacantes.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado actual de la vacante")
public enum EstadoVacante {
    ABIERTA,
    CERRADA
}
