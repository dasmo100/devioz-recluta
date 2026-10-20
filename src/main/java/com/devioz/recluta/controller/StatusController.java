package com.devioz.recluta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/status")
@Tag(name = "Estado del Sistema", description = "Endpoints para verificación del estado del servicio")
public class StatusController {

    @GetMapping
    @Operation(summary = "Verificar estado de la API", description = "Retorna el estado operativo actual y timestamp del servicio.")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "application", "devioz-recluta",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "message", "API DeviozRecluta Express operativa y conectada correctamente"
        ));
    }
}
