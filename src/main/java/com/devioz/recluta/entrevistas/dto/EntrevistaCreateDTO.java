package com.devioz.recluta.entrevistas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "DTO de solicitud para agendar una nueva entrevista")
public record EntrevistaCreateDTO(

        @NotBlank(message = "El nombre del entrevistador es obligatorio")
        @Schema(description = "Nombre o identificador del entrevistador", example = "Ing. Roberto Gómez")
        String entrevistador,

        @NotNull(message = "La fecha y hora son obligatorias")
        @Schema(description = "Fecha y hora programada para la entrevista (ISO 8601)", example = "2026-10-25T15:30:00")
        LocalDateTime fechaHora,

        @NotNull(message = "La duración es obligatoria")
        @Min(value = 15, message = "La duración mínima es de 15 minutos")
        @Schema(description = "Duración estimada en minutos (mínimo 15)", example = "45")
        Integer duracionMinutos,

        @Schema(description = "Comentarios, indicaciones o enlace de la reunión virtual", example = "Enlace Google Meet: https://meet.google.com/xyz-abcd-efg")
        String comentario
) {
}
