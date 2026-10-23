package com.devioz.recluta.postulaciones.dto;

import com.devioz.recluta.postulaciones.entity.EstadoPostulacion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO de solicitud para realizar una transición de estado en la postulación")
public record CambioEstadoDTO(

        @NotNull(message = "El nuevo estado es obligatorio")
        @Schema(description = "Nuevo estado de la postulación", example = "EN_REVISION")
        EstadoPostulacion nuevoEstado,

        @NotBlank(message = "El comentario es obligatorio al cambiar de estado")
        @Schema(description = "Motivo o justificación del cambio de estado", example = "El perfil cumple con los requisitos iniciales")
        String comentario
) {
}
