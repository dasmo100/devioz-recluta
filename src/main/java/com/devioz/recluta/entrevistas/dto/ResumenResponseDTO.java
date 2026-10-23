package com.devioz.recluta.entrevistas.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "DTO de respuesta con el resumen global de vacantes, postulaciones y entrevistas de hoy")
public record ResumenResponseDTO(

        @Schema(description = "Total de vacantes actualmente en estado ABIERTA", example = "3")
        long vacantesAbiertas,

        @Schema(description = "Conteo de postulaciones agrupadas por cada uno de los 5 estados del proceso",
                example = "{\"POSTULADO\": 4, \"EN_REVISION\": 2, \"ENTREVISTA\": 1, \"CONTRATADO\": 1, \"RECHAZADO\": 0}")
        Map<String, Long> postulacionesPorEstado,

        @Schema(description = "Lista de entrevistas programadas o realizadas para el día de hoy")
        List<EntrevistaResponseDTO> entrevistasDeHoy
) {
}
