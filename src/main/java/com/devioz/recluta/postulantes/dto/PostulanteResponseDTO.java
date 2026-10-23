package com.devioz.recluta.postulantes.dto;

import com.devioz.recluta.postulantes.entity.Postulante;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DTO de respuesta con la información completa del postulante")
public record PostulanteResponseDTO(

        @Schema(description = "Identificador único del postulante", example = "1")
        Long id,

        @Schema(description = "Nombres del postulante", example = "Juan Carlos")
        String nombres,

        @Schema(description = "Apellidos del postulante", example = "Pérez Gómez")
        String apellidos,

        @Schema(description = "Documento Nacional de Identidad", example = "71234567")
        String dni,

        @Schema(description = "Correo electrónico del postulante", example = "juan.perez@example.com")
        String email,

        @Schema(description = "Teléfono o celular de contacto", example = "987654321")
        String telefono,

        @Schema(description = "Años de experiencia profesional", example = "4")
        Integer aniosExperiencia,

        @Schema(description = "Habilidades técnicas", example = "Java 21, Spring Boot, MySQL, Git, Docker")
        String habilidades,

        @Schema(description = "Fecha de registro en el sistema", example = "2026-10-23")
        LocalDate fechaRegistro
) {

    public static PostulanteResponseDTO fromEntity(Postulante postulante) {
        return new PostulanteResponseDTO(
                postulante.getId(),
                postulante.getNombres(),
                postulante.getApellidos(),
                postulante.getDni(),
                postulante.getEmail(),
                postulante.getTelefono(),
                postulante.getAniosExperiencia(),
                postulante.getHabilidades(),
                postulante.getFechaRegistro()
        );
    }
}
