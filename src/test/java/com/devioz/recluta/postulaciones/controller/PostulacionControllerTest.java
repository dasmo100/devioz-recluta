package com.devioz.recluta.postulaciones.controller;

import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.GlobalExceptionHandler;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.postulaciones.dto.CambioEstadoDTO;
import com.devioz.recluta.postulaciones.dto.HistorialEstadoResponseDTO;
import com.devioz.recluta.postulaciones.dto.PostulacionCreateDTO;
import com.devioz.recluta.postulaciones.dto.PostulacionResponseDTO;
import com.devioz.recluta.postulaciones.entity.EstadoPostulacion;
import com.devioz.recluta.postulaciones.service.PostulacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostulacionController.class)
@Import(GlobalExceptionHandler.class)
class PostulacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostulacionService postulacionService;

    private PostulacionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new PostulacionResponseDTO(
                1L,
                10L,
                "Carlos Gómez",
                "12345678",
                20L,
                "Backend Java",
                EstadoPostulacion.POSTULADO,
                LocalDateTime.of(2026, 10, 23, 10, 0)
        );
    }

    @Test
    @DisplayName("POST /api/vacantes/{id}/postulaciones debe retornar 201 al crear exitosamente")
    void crearPostulacionDebeRetornar201() throws Exception {
        PostulacionCreateDTO request = new PostulacionCreateDTO(10L, "Postulación con experiencia");
        when(postulacionService.crear(eq(20L), any(PostulacionCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/vacantes/20/postulaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("POSTULADO"));
    }

    @Test
    @DisplayName("POST /api/vacantes/{id}/postulaciones debe retornar 400 si falta postulanteId")
    void crearPostulacionSinPostulanteIdDebeRetornar400() throws Exception {
        PostulacionCreateDTO requestInvalido = new PostulacionCreateDTO(null, "Sin ID");

        mockMvc.perform(post("/api/vacantes/20/postulaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /api/vacantes/{id}/postulaciones debe retornar 400 si la vacante está cerrada")
    void crearPostulacionEnVacanteCerradaDebeRetornar400() throws Exception {
        PostulacionCreateDTO request = new PostulacionCreateDTO(10L, "Intento");
        when(postulacionService.crear(eq(20L), any(PostulacionCreateDTO.class)))
                .thenThrow(new BusinessRuleException("No se puede postular a una vacante que se encuentra cerrada"));

        mockMvc.perform(post("/api/vacantes/20/postulaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("No se puede postular a una vacante que se encuentra cerrada"));
    }

    @Test
    @DisplayName("GET /api/vacantes/{id}/postulaciones debe retornar 200 y listado")
    void listarPorVacanteDebeRetornar200() throws Exception {
        when(postulacionService.listarPorVacante(20L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/vacantes/20/postulaciones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].postulanteId").value(10L));
    }

    @Test
    @DisplayName("PATCH /api/postulaciones/{id}/estado debe retornar 200 al actualizar estado")
    void cambiarEstadoDebeRetornar200() throws Exception {
        CambioEstadoDTO request = new CambioEstadoDTO(EstadoPostulacion.EN_REVISION, "Revisión aprobada");
        PostulacionResponseDTO responseEnRevision = new PostulacionResponseDTO(
                1L, 10L, "Carlos Gómez", "12345678", 20L, "Backend Java", EstadoPostulacion.EN_REVISION, LocalDateTime.now()
        );
        when(postulacionService.cambiarEstado(eq(1L), any(CambioEstadoDTO.class))).thenReturn(responseEnRevision);

        mockMvc.perform(patch("/api/postulaciones/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));
    }

    @Test
    @DisplayName("PATCH /api/postulaciones/{id}/estado debe retornar 400 si salto de etapa es inválido")
    void cambiarEstadoInvalidoDebeRetornar400() throws Exception {
        CambioEstadoDTO request = new CambioEstadoDTO(EstadoPostulacion.CONTRATADO, "Salto directo");
        when(postulacionService.cambiarEstado(eq(1L), any(CambioEstadoDTO.class)))
                .thenThrow(new BusinessRuleException("Transición no permitida: no se puede pasar del estado 'POSTULADO' a 'CONTRATADO'"));

        mockMvc.perform(patch("/api/postulaciones/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Transición no permitida: no se puede pasar del estado 'POSTULADO' a 'CONTRATADO'"));
    }

    @Test
    @DisplayName("GET /api/postulaciones/{id}/historial debe retornar 200 y bitácora")
    void consultarHistorialDebeRetornar200() throws Exception {
        HistorialEstadoResponseDTO h = new HistorialEstadoResponseDTO(
                1L, 1L, null, EstadoPostulacion.POSTULADO, LocalDateTime.now(), "Registro inicial"
        );
        when(postulacionService.consultarHistorial(1L)).thenReturn(List.of(h));

        mockMvc.perform(get("/api/postulaciones/1/historial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].estadoNuevo").value("POSTULADO"));
    }
}
