package com.devioz.recluta.vacantes.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nivel de experiencia requerido para la vacante")
public enum Seniority {
    JUNIOR,
    SEMI_SENIOR,
    SENIOR
}
