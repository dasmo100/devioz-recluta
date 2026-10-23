package com.devioz.recluta.postulaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO de solicitud para registrar una postulación a una vacante")
public record PostulacionCreateDTO(

        @NotNull(message = "El ID del postulante es obligatorio")
        @Schema(description = "Identificador único del postulante que postula a la vacante", example = "1")
        Long postulanteId,

        @Schema(description = "Comentario u observaciones opcionales del postulante al momento de postular", example = "Postulación espontánea vía portal web")
        String comentario
) {
}
