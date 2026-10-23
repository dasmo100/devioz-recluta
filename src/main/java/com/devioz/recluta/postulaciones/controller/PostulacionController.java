package com.devioz.recluta.postulaciones.controller;

import com.devioz.recluta.postulaciones.dto.CambioEstadoDTO;
import com.devioz.recluta.postulaciones.dto.HistorialEstadoResponseDTO;
import com.devioz.recluta.postulaciones.dto.PostulacionCreateDTO;
import com.devioz.recluta.postulaciones.dto.PostulacionResponseDTO;
import com.devioz.recluta.postulaciones.service.PostulacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Postulaciones", description = "Endpoints para la gestión del ciclo de vida de postulaciones y seguimiento de estados")
public class PostulacionController {

    private final PostulacionService postulacionService;

    @PostMapping("/api/vacantes/{id}/postulaciones")
    @Operation(
            summary = "Registrar postulación a una vacante",
            description = "Permite a un candidato postular a una vacante abierta. Valida que la vacante no esté cerrada y que no existan postulaciones duplicadas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Postulación registrada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostulacionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Vacante cerrada, postulante duplicado o datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Vacante o postulante no encontrado")
    })
    public ResponseEntity<PostulacionResponseDTO> crear(
            @Parameter(description = "Identificador único de la vacante", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody PostulacionCreateDTO request) {
        PostulacionResponseDTO nuevaPostulacion = postulacionService.crear(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/postulaciones/{id}")
                .buildAndExpand(nuevaPostulacion.id())
                .toUri();
        return ResponseEntity.created(location).body(nuevaPostulacion);
    }

    @GetMapping("/api/vacantes/{id}/postulaciones")
    @Operation(
            summary = "Listar postulaciones de una vacante",
            description = "Obtiene todas las postulaciones asociadas a la vacante especificada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de postulaciones recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostulacionResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada")
    })
    public ResponseEntity<List<PostulacionResponseDTO>> listarPorVacante(
            @Parameter(description = "Identificador único de la vacante", example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(postulacionService.listarPorVacante(id));
    }

    @PatchMapping("/api/postulaciones/{id}/estado")
    @Operation(
            summary = "Cambiar estado de una postulación",
            description = "Avanza o rechaza una postulación siguiendo el flujo de estados estricto. Si se contrata, la vacante pasa automáticamente a estado CERRADA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de la postulación actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostulacionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Transición de estado no permitida o postulación en estado final"),
            @ApiResponse(responseCode = "404", description = "Postulación no encontrada")
    })
    public ResponseEntity<PostulacionResponseDTO> cambiarEstado(
            @Parameter(description = "Identificador único de la postulación", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody CambioEstadoDTO request) {
        return ResponseEntity.ok(postulacionService.cambiarEstado(id, request));
    }

    @GetMapping("/api/postulaciones/{id}/historial")
    @Operation(
            summary = "Consultar historial de estados de una postulación",
            description = "Retorna la bitácora completa de cambios de estado ordenados cronológicamente desde su registro inicial."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial recuperado exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = HistorialEstadoResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Postulación no encontrada")
    })
    public ResponseEntity<List<HistorialEstadoResponseDTO>> consultarHistorial(
            @Parameter(description = "Identificador único de la postulación", example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(postulacionService.consultarHistorial(id));
    }
}
