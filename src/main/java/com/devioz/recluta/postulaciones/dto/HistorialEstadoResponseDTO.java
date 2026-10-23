package com.devioz.recluta.postulaciones.dto;

import com.devioz.recluta.postulaciones.entity.EstadoPostulacion;
import com.devioz.recluta.postulaciones.entity.HistorialEstado;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO de respuesta para los eventos de cambio de estado en el historial")
public record HistorialEstadoResponseDTO(

        @Schema(description = "Identificador único del registro de historial", example = "1")
        Long id,

        @Schema(description = "Identificador de la postulación asociada", example = "1")
        Long postulacionId,

        @Schema(description = "Estado previo antes del cambio (null en registro inicial)", example = "POSTULADO")
        EstadoPostulacion estadoAnterior,

        @Schema(description = "Nuevo estado asignado", example = "EN_REVISION")
        EstadoPostulacion estadoNuevo,

        @Schema(description = "Fecha y hora del cambio de estado", example = "2026-10-23T11:00:00")
        LocalDateTime fecha,

        @Schema(description = "Comentario o justificación del cambio", example = "Perfil calificado para revisión")
        String comentario
) {

    public static HistorialEstadoResponseDTO fromEntity(HistorialEstado historial) {
        Long postulacionId = historial.getPostulacion() != null ? historial.getPostulacion().getId() : null;
        return new HistorialEstadoResponseDTO(
                historial.getId(),
                postulacionId,
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getFecha(),
                historial.getComentario()
        );
    }
}
