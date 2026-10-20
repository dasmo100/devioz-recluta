package com.devioz.recluta.vacantes.dto;

import com.devioz.recluta.vacantes.model.Modalidad;
import com.devioz.recluta.vacantes.model.Seniority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "DTO de solicitud para crear o actualizar una vacante")
public record VacanteRequestDTO(

        @NotBlank(message = "El título de la vacante es obligatorio")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        @Schema(description = "Título del puesto de la vacante", example = "Desarrollador Java Senior")
        String titulo,

        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 150, message = "El cliente no puede superar los 150 caracteres")
        @Schema(description = "Empresa o cliente solicitante", example = "Banco de Crédito")
        String cliente,

        @NotNull(message = "El seniority es obligatorio")
        @Schema(description = "Nivel de experiencia requerido", example = "SENIOR")
        Seniority seniority,

        @NotNull(message = "El salario mínimo es obligatorio")
        @PositiveOrZero(message = "El salario mínimo debe ser mayor o igual a cero")
        @Schema(description = "Rango salarial mínimo", example = "6000.00")
        BigDecimal salarioMinimo,

        @NotNull(message = "El salario máximo es obligatorio")
        @PositiveOrZero(message = "El salario máximo debe ser mayor o igual a cero")
        @Schema(description = "Rango salarial máximo", example = "9000.00")
        BigDecimal salarioMaximo,

        @NotNull(message = "La modalidad es obligatoria")
        @Schema(description = "Modalidad laboral", example = "REMOTO")
        Modalidad modalidad
) {

    @AssertTrue(message = "El salario mínimo debe ser menor o igual al salario máximo")
    public boolean isSalarioValido() {
        if (salarioMinimo == null || salarioMaximo == null) {
            return true;
        }
        return salarioMinimo.compareTo(salarioMaximo) <= 0;
    }
}
