package com.devioz.recluta.vacantes.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Modalidad de trabajo de la vacante")
public enum Modalidad {
    PRESENCIAL,
    REMOTO,
    HIBRIDO
}
