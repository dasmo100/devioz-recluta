package com.devioz.recluta.vacantes.controller;

import com.devioz.recluta.vacantes.dto.VacanteRequestDTO;
import com.devioz.recluta.vacantes.dto.VacanteResponseDTO;
import com.devioz.recluta.vacantes.model.EstadoVacante;
import com.devioz.recluta.vacantes.service.VacanteService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vacantes")
@RequiredArgsConstructor
@Tag(name = "Vacantes", description = "Endpoints para la gestión integral de vacantes laborales")
public class VacanteController {

    private final VacanteService vacanteService;

    @GetMapping
    @Operation(summary = "Listar todas las vacantes", description = "Retorna el catálogo de vacantes registradas, con posibilidad de filtrar por su estado actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de vacantes recuperada con éxito",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VacanteResponseDTO.class))))
    })
    public ResponseEntity<List<VacanteResponseDTO>> listar(
            @Parameter(description = "Filtro opcional por estado de la vacante (ABIERTA o CERRADA)")
            @RequestParam(required = false) EstadoVacante estado) {
        return ResponseEntity.ok(vacanteService.listar(estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vacante por ID", description = "Obtiene los detalles específicos de una vacante a partir de su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacante encontrada con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VacanteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada con el identificador proporcionado")
    })
    public ResponseEntity<VacanteResponseDTO> obtenerPorId(
            @Parameter(description = "Identificador numérico de la vacante", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(vacanteService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nueva vacante", description = "Registra una nueva vacante con estado inicial ABIERTA. Valida que el salario mínimo sea menor o igual al máximo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vacante creada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VacanteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o salario mínimo superior al máximo")
    })
    public ResponseEntity<VacanteResponseDTO> crear(
            @Valid @RequestBody VacanteRequestDTO request) {
        VacanteResponseDTO nuevaVacante = vacanteService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevaVacante.id())
                .toUri();
        return ResponseEntity.created(location).body(nuevaVacante);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vacante existente", description = "Modifica los campos de una vacante existente. No se permite editar vacantes en estado CERRADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacante actualizada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VacanteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o intento de modificar una vacante CERRADA"),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada")
    })
    public ResponseEntity<VacanteResponseDTO> actualizar(
            @Parameter(description = "Identificador numérico de la vacante a editar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody VacanteRequestDTO request) {
        return ResponseEntity.ok(vacanteService.actualizar(id, request));
    }

    @PatchMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar vacante", description = "Cambia el estado de una vacante abierta a CERRADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacante cerrada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VacanteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "La vacante ya se encuentra cerrada"),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada")
    })
    public ResponseEntity<VacanteResponseDTO> cerrar(
            @Parameter(description = "Identificador numérico de la vacante a cerrar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(vacanteService.cerrar(id));
    }
}
