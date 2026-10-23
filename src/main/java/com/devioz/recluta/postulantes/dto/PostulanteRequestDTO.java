package com.devioz.recluta.postulantes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "DTO de solicitud para el registro o actualización de un postulante")
public record PostulanteRequestDTO(

        @NotBlank(message = "Los nombres son obligatorios")
        @Schema(description = "Nombres del postulante", example = "Juan Carlos")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Schema(description = "Apellidos del postulante", example = "Pérez Gómez")
        String apellidos,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos numéricos")
        @Schema(description = "Documento Nacional de Identidad de 8 dígitos", example = "71234567")
        String dni,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Schema(description = "Correo electrónico del postulante", example = "juan.perez@example.com")
        String email,

        @Schema(description = "Teléfono o celular de contacto", example = "987654321")
        String telefono,

        @NotNull(message = "Los años de experiencia son obligatorios")
        @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
        @Schema(description = "Años de experiencia profesional relevante", example = "4")
        Integer aniosExperiencia,

        @NotBlank(message = "Las habilidades son obligatorias")
        @Schema(description = "Habilidades técnicas y conocimientos separados por comas", example = "Java 21, Spring Boot, MySQL, Git, Docker")
        String habilidades
) {
}
