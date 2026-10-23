package com.devioz.recluta.entrevistas.dto;

import com.devioz.recluta.entrevistas.entity.EstadoEntrevista;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO de solicitud para registrar el resultado de una entrevista")
public record EntrevistaResultadoDTO(

        @NotNull(message = "El estado es obligatorio")
        @Schema(description = "Nuevo estado de la entrevista (REALIZADA o CANCELADA)", example = "REALIZADA")
        EstadoEntrevista estado,

        @Min(value = 1, message = "El puntaje mínimo es 1")
        @Max(value = 5, message = "El puntaje máximo es 5")
        @Schema(description = "Puntaje obtenido de 1 a 5 (obligatorio si el estado es REALIZADA)", example = "4")
        Integer puntaje,

        @Schema(description = "Comentarios u observaciones sobre el desempeño o motivo de cancelación", example = "Excelente dominio técnico de Spring Boot y arquitectura de software.")
        String comentario
) {
}
