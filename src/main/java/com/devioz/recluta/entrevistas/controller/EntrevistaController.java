package com.devioz.recluta.entrevistas.controller;

import com.devioz.recluta.entrevistas.dto.EntrevistaCreateDTO;
import com.devioz.recluta.entrevistas.dto.EntrevistaResponseDTO;
import com.devioz.recluta.entrevistas.dto.EntrevistaResultadoDTO;
import com.devioz.recluta.entrevistas.dto.ResumenResponseDTO;
import com.devioz.recluta.entrevistas.service.EntrevistaService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Entrevistas y Resumen", description = "Endpoints para la gestión de entrevistas de candidatos y resumen ejecutivo de reclutamiento")
public class EntrevistaController {

    private final EntrevistaService entrevistaService;

    @PostMapping("/api/postulaciones/{id}/entrevistas")
    @Operation(
            summary = "Agendar entrevista para una postulación",
            description = "Programa una nueva entrevista técnica o RRHH. Solo se permite si la postulación está en estado ENTREVISTA, no se agendan fechas pasadas y no hay cruce de horarios para el entrevistador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entrevista programada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntrevistaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Regla de negocio no cumplida (estado inválido, fecha pasada o cruce de horario)"),
            @ApiResponse(responseCode = "404", description = "Postulación no encontrada")
    })
    public ResponseEntity<EntrevistaResponseDTO> agendar(
            @Parameter(description = "Identificador único de la postulación", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody EntrevistaCreateDTO request) {

        EntrevistaResponseDTO nuevaEntrevista = entrevistaService.agendar(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/entrevistas/{id}")
                .buildAndExpand(nuevaEntrevista.id())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(nuevaEntrevista);
    }

    @PatchMapping("/api/entrevistas/{id}/resultado")
    @Operation(
            summary = "Registrar resultado de una entrevista",
            description = "Actualiza el estado de la entrevista a REALIZADA (exige puntaje de 1 a 5) o CANCELADA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado registrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntrevistaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Entrevista ya concluida, estado inválido o falta puntaje obligatorio"),
            @ApiResponse(responseCode = "404", description = "Entrevista no encontrada")
    })
    public ResponseEntity<EntrevistaResponseDTO> registrarResultado(
            @Parameter(description = "Identificador único de la entrevista", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody EntrevistaResultadoDTO request) {

        return ResponseEntity.ok(entrevistaService.registrarResultado(id, request));
    }

    @GetMapping("/api/entrevistas")
    @Operation(
            summary = "Listar entrevistas por fecha",
            description = "Devuelve todas las entrevistas filtradas por fecha (formato YYYY-MM-DD). Si el parámetro de fecha no se envía, retorna todas las entrevistas ordenadas por fecha/hora ascendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de entrevistas recuperado exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EntrevistaResponseDTO.class))))
    })
    public ResponseEntity<List<EntrevistaResponseDTO>> listarPorFecha(
            @Parameter(description = "Fecha de búsqueda (opcional, formato AAAA-MM-DD)", example = "2026-10-25")
            @RequestParam(name = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(entrevistaService.listarPorFecha(fecha));
    }

    @GetMapping("/api/resumen")
    @Operation(
            summary = "Obtener resumen ejecutivo del proceso",
            description = "Proporciona el conteo de vacantes abiertas, el desglose de postulaciones en cada uno de los 5 estados del embudo y las entrevistas programadas o realizadas para el día de hoy."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen del reclutamiento generado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResumenResponseDTO.class)))
    })
    public ResponseEntity<ResumenResponseDTO> obtenerResumen() {
        return ResponseEntity.ok(entrevistaService.obtenerResumen());
    }
}
