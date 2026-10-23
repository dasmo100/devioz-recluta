package com.devioz.recluta.postulaciones.dto;

import com.devioz.recluta.postulaciones.entity.EstadoPostulacion;
import com.devioz.recluta.postulaciones.entity.Postulacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO de respuesta con el detalle de la postulación")
public record PostulacionResponseDTO(

        @Schema(description = "Identificador único de la postulación", example = "1")
        Long id,

        @Schema(description = "ID del postulante asociado", example = "1")
        Long postulanteId,

        @Schema(description = "Nombre completo del postulante", example = "Juan Carlos Pérez Gómez")
        String nombreCompletoPostulante,

        @Schema(description = "DNI del postulante", example = "71234567")
        String dniPostulante,

        @Schema(description = "ID de la vacante a la que postula", example = "1")
        Long vacanteId,

        @Schema(description = "Título de la vacante", example = "Desarrollador Java Senior")
        String tituloVacante,

        @Schema(description = "Estado actual de la postulación", example = "POSTULADO")
        EstadoPostulacion estado,

        @Schema(description = "Fecha y hora del registro de la postulación", example = "2026-10-23T10:15:30")
        LocalDateTime fecha
) {

    public static PostulacionResponseDTO fromEntity(Postulacion postulacion) {
        String nombreCompleto = null;
        String dni = null;
        Long postId = null;

        if (postulacion.getPostulante() != null) {
            postId = postulacion.getPostulante().getId();
            nombreCompleto = (postulacion.getPostulante().getNombres() + " " + postulacion.getPostulante().getApellidos()).trim();
            dni = postulacion.getPostulante().getDni();
        }

        Long vacId = null;
        String titulo = null;

        if (postulacion.getVacante() != null) {
            vacId = postulacion.getVacante().getId();
            titulo = postulacion.getVacante().getTitulo();
        }

        return new PostulacionResponseDTO(
                postulacion.getId(),
                postId,
                nombreCompleto,
                dni,
                vacId,
                titulo,
                postulacion.getEstado(),
                postulacion.getFecha()
        );
    }
}
