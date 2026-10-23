package com.devioz.recluta.entrevistas.dto;

import com.devioz.recluta.entrevistas.entity.Entrevista;
import com.devioz.recluta.entrevistas.entity.EstadoEntrevista;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO de respuesta con el detalle de la entrevista")
public record EntrevistaResponseDTO(

        @Schema(description = "Identificador único de la entrevista", example = "1")
        Long id,

        @Schema(description = "ID de la postulación asociada", example = "1")
        Long postulacionId,

        @Schema(description = "Nombre completo del postulante entrevistado", example = "Carlos Mendoza Quispe")
        String nombrePostulante,

        @Schema(description = "Título de la vacante a la que postula", example = "Desarrollador Java Senior")
        String tituloVacante,

        @Schema(description = "Nombre del entrevistador", example = "Ing. Roberto Gómez")
        String entrevistador,

        @Schema(description = "Fecha y hora programada de la entrevista", example = "2026-10-25T15:30:00")
        LocalDateTime fechaHora,

        @Schema(description = "Duración programada en minutos", example = "45")
        Integer duracionMinutos,

        @Schema(description = "Estado actual de la entrevista", example = "PROGRAMADA")
        EstadoEntrevista estado,

        @Schema(description = "Puntaje asignado (1 a 5)", example = "4")
        Integer puntaje,

        @Schema(description = "Comentario de la entrevista", example = "Excelente dominio técnico")
        String comentario
) {

    public static EntrevistaResponseDTO fromEntity(Entrevista entrevista) {
        Long postId = null;
        String postulanteNombre = null;
        String vacanteTitulo = null;

        if (entrevista.getPostulacion() != null) {
            postId = entrevista.getPostulacion().getId();
            if (entrevista.getPostulacion().getPostulante() != null) {
                String nombres = entrevista.getPostulacion().getPostulante().getNombres();
                String apellidos = entrevista.getPostulacion().getPostulante().getApellidos();
                postulanteNombre = ((nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "")).trim();
            }
            if (entrevista.getPostulacion().getVacante() != null) {
                vacanteTitulo = entrevista.getPostulacion().getVacante().getTitulo();
            }
        }

        return new EntrevistaResponseDTO(
                entrevista.getId(),
                postId,
                postulanteNombre,
                vacanteTitulo,
                entrevista.getEntrevistador(),
                entrevista.getFechaHora(),
                entrevista.getDuracionMinutos(),
                entrevista.getEstado(),
                entrevista.getPuntaje(),
                entrevista.getComentario()
        );
    }
}
