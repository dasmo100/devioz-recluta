package com.devioz.recluta.vacantes.dto;

import com.devioz.recluta.vacantes.model.EstadoVacante;
import com.devioz.recluta.vacantes.model.Modalidad;
import com.devioz.recluta.vacantes.model.Seniority;
import com.devioz.recluta.vacantes.model.Vacante;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO de respuesta con la información completa de una vacante")
public record VacanteResponseDTO(

        @Schema(description = "Identificador único de la vacante", example = "1")
        Long id,

        @Schema(description = "Título del puesto de la vacante", example = "Desarrollador Java Senior")
        String titulo,

        @Schema(description = "Empresa o cliente solicitante", example = "Banco de Crédito")
        String cliente,

        @Schema(description = "Nivel de experiencia requerido", example = "SENIOR")
        Seniority seniority,

        @Schema(description = "Rango salarial mínimo", example = "6000.00")
        BigDecimal salarioMinimo,

        @Schema(description = "Rango salarial máximo", example = "9000.00")
        BigDecimal salarioMaximo,

        @Schema(description = "Modalidad laboral", example = "REMOTO")
        Modalidad modalidad,

        @Schema(description = "Estado de la vacante", example = "ABIERTA")
        EstadoVacante estado
) {

    public static VacanteResponseDTO fromEntity(Vacante vacante) {
        return new VacanteResponseDTO(
                vacante.getId(),
                vacante.getTitulo(),
                vacante.getCliente(),
                vacante.getSeniority(),
                vacante.getSalarioMinimo(),
                vacante.getSalarioMaximo(),
                vacante.getModalidad(),
                vacante.getEstado()
        );
    }
}
