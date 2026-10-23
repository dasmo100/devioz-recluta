package com.devioz.recluta.postulantes.controller;

import com.devioz.recluta.postulantes.dto.PostulanteRequestDTO;
import com.devioz.recluta.postulantes.dto.PostulanteResponseDTO;
import com.devioz.recluta.postulantes.service.PostulanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/postulantes")
@RequiredArgsConstructor
@Tag(name = "Postulantes", description = "Endpoints para la gestión integral de candidatos y postulantes")
public class PostulanteController {

    private final PostulanteService postulanteService;

    @GetMapping
    @Operation(
            summary = "Listar o buscar postulantes paginados",
            description = "Retorna una página de postulantes registrados. Permite filtrar por coincidencia en nombres, apellidos o DNI mediante el parámetro opcional 'q'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de postulantes obtenida con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<PostulanteResponseDTO>> buscar(
            @Parameter(description = "Término de búsqueda opcional (nombres, apellidos o DNI)", example = "Juan")
            @RequestParam(required = false) String q,
            @Parameter(description = "Número de página a consultar (0-indexada)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postulanteService.buscar(q, page, size));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener postulante por ID",
            description = "Obtiene los detalles específicos de un postulante a partir de su ID único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulante encontrado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostulanteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Postulante no encontrado con el identificador proporcionado")
    })
    public ResponseEntity<PostulanteResponseDTO> obtenerPorId(
            @Parameter(description = "Identificador único del postulante", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(postulanteService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(
            summary = "Registrar nuevo postulante",
            description = "Registra un nuevo postulante en la base de datos validando que el DNI y el email no existan previamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Postulante registrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostulanteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o reglas de negocio incumplidas (DNI/email duplicados)")
    })
    public ResponseEntity<PostulanteResponseDTO> crear(
            @Valid @RequestBody PostulanteRequestDTO request) {
        PostulanteResponseDTO nuevoPostulante = postulanteService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevoPostulante.id())
                .toUri();
        return ResponseEntity.created(location).body(nuevoPostulante);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar postulante existente",
            description = "Modifica los datos de un postulante existente. Valida que el nuevo DNI o email no pertenezcan a otro postulante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulante actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostulanteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o DNI/email en uso por otro postulante"),
            @ApiResponse(responseCode = "404", description = "Postulante no encontrado")
    })
    public ResponseEntity<PostulanteResponseDTO> actualizar(
            @Parameter(description = "Identificador único del postulante a editar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PostulanteRequestDTO request) {
        return ResponseEntity.ok(postulanteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar postulante",
            description = "Elimina un postulante del sistema. No se permite eliminar si el postulante cuenta con postulaciones registradas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Postulante eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar al postulante porque tiene postulaciones registradas"),
            @ApiResponse(responseCode = "404", description = "Postulante no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador único del postulante a eliminar", example = "1")
            @PathVariable Long id) {
        postulanteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
