package com.devioz.recluta.entrevistas.controller;

import com.devioz.recluta.entrevistas.dto.EntrevistaCreateDTO;
import com.devioz.recluta.entrevistas.dto.EntrevistaResponseDTO;
import com.devioz.recluta.entrevistas.dto.EntrevistaResultadoDTO;
import com.devioz.recluta.entrevistas.dto.ResumenResponseDTO;
import com.devioz.recluta.entrevistas.entity.EstadoEntrevista;
import com.devioz.recluta.entrevistas.service.EntrevistaService;
import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.GlobalExceptionHandler;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EntrevistaController.class)
@Import(GlobalExceptionHandler.class)
class EntrevistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntrevistaService entrevistaService;

    private EntrevistaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new EntrevistaResponseDTO(
                1L,
                10L,
                "Carlos Mendoza Quispe",
                "Backend Developer Java",
                "Ing. Roberto Gómez",
                LocalDateTime.of(2026, 10, 25, 10, 0),
                45,
                EstadoEntrevista.PROGRAMADA,
                null,
                "Reunión Google Meet"
        );
    }

    @Test
    @DisplayName("POST /api/postulaciones/{id}/entrevistas debe retornar 201 Created cuando los datos son válidos")
    void agendar_Retorna201Created() throws Exception {
        EntrevistaCreateDTO createDTO = new EntrevistaCreateDTO(
                "Ing. Roberto Gómez",
                LocalDateTime.of(2026, 10, 25, 10, 0),
                45,
                "Reunión Google Meet"
        );

        when(entrevistaService.agendar(eq(10L), any(EntrevistaCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/postulaciones/10/entrevistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.postulacionId").value(10))
                .andExpect(jsonPath("$.entrevistador").value("Ing. Roberto Gómez"))
                .andExpect(jsonPath("$.duracionMinutos").value(45))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    @DisplayName("POST /api/postulaciones/{id}/entrevistas debe retornar 400 Bad Request cuando los datos son inválidos")
    void agendar_Retorna400BadRequest_CuandoDatosInvalidos() throws Exception {
        // Entrevistador en blanco y duración menor a 15
        EntrevistaCreateDTO invalidoDTO = new EntrevistaCreateDTO(
                "",
                null,
                10,
                null
        );

        mockMvc.perform(post("/api/postulaciones/10/entrevistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /api/postulaciones/{id}/entrevistas debe retornar 400 Bad Request cuando se incumple una regla de negocio")
    void agendar_Retorna400BadRequest_CuandoReglaDeNegocioIncumplida() throws Exception {
        EntrevistaCreateDTO createDTO = new EntrevistaCreateDTO(
                "Ing. Roberto Gómez",
                LocalDateTime.of(2026, 10, 25, 10, 0),
                45,
                null
        );

        when(entrevistaService.agendar(eq(10L), any(EntrevistaCreateDTO.class)))
                .thenThrow(new BusinessRuleException("El entrevistador ya tiene una entrevista programada en ese rango de horario"));

        mockMvc.perform(post("/api/postulaciones/10/entrevistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El entrevistador ya tiene una entrevista programada en ese rango de horario"));
    }

    @Test
    @DisplayName("PATCH /api/entrevistas/{id}/resultado debe retornar 200 OK cuando se registra el resultado")
    void registrarResultado_Retorna200Ok() throws Exception {
        EntrevistaResultadoDTO resultadoDTO = new EntrevistaResultadoDTO(
                EstadoEntrevista.REALIZADA,
                5,
                "Excelente candidato"
        );

        EntrevistaResponseDTO resultadoResponse = new EntrevistaResponseDTO(
                1L,
                10L,
                "Carlos Mendoza Quispe",
                "Backend Developer Java",
                "Ing. Roberto Gómez",
                LocalDateTime.of(2026, 10, 25, 10, 0),
                45,
                EstadoEntrevista.REALIZADA,
                5,
                "Excelente candidato"
        );

        when(entrevistaService.registrarResultado(eq(1L), any(EntrevistaResultadoDTO.class)))
                .thenReturn(resultadoResponse);

        mockMvc.perform(patch("/api/entrevistas/1/resultado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultadoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("REALIZADA"))
                .andExpect(jsonPath("$.puntaje").value(5))
                .andExpect(jsonPath("$.comentario").value("Excelente candidato"));
    }

    @Test
    @DisplayName("PATCH /api/entrevistas/{id}/resultado debe retornar 400 Bad Request cuando falta el puntaje obligatorio")
    void registrarResultado_Retorna400BadRequest_CuandoFaltaPuntaje() throws Exception {
        EntrevistaResultadoDTO resultadoDTO = new EntrevistaResultadoDTO(
                EstadoEntrevista.REALIZADA,
                null,
                "Faltó calificar"
        );

        when(entrevistaService.registrarResultado(eq(1L), any(EntrevistaResultadoDTO.class)))
                .thenThrow(new BusinessRuleException("El puntaje (1 a 5) es obligatorio al marcar la entrevista como REALIZADA"));

        mockMvc.perform(patch("/api/entrevistas/1/resultado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultadoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El puntaje (1 a 5) es obligatorio al marcar la entrevista como REALIZADA"));
    }

    @Test
    @DisplayName("GET /api/entrevistas debe retornar 200 OK con la lista de entrevistas filtradas por fecha")
    void listarPorFecha_Retorna200Ok() throws Exception {
        when(entrevistaService.listarPorFecha(LocalDate.of(2026, 10, 25)))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/entrevistas")
                        .param("fecha", "2026-10-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].entrevistador").value("Ing. Roberto Gómez"));
    }

    @Test
    @DisplayName("GET /api/entrevistas sin parámetro fecha debe retornar 200 OK con todas las entrevistas")
    void listarPorFecha_SinParametro_Retorna200Ok() throws Exception {
        when(entrevistaService.listarPorFecha(null))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/entrevistas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/resumen debe retornar 200 OK con las vacantes abiertas, conteo por estados y entrevistas de hoy")
    void obtenerResumen_Retorna200Ok() throws Exception {
        Map<String, Long> estados = Map.of(
                "POSTULADO", 5L,
                "EN_REVISION", 2L,
                "ENTREVISTA", 1L,
                "CONTRATADO", 1L,
                "RECHAZADO", 0L
        );

        ResumenResponseDTO resumenResponse = new ResumenResponseDTO(
                3L,
                estados,
                List.of(responseDTO)
        );

        when(entrevistaService.obtenerResumen()).thenReturn(resumenResponse);

        mockMvc.perform(get("/api/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacantesAbiertas").value(3))
                .andExpect(jsonPath("$.postulacionesPorEstado.POSTULADO").value(5))
                .andExpect(jsonPath("$.postulacionesPorEstado.EN_REVISION").value(2))
                .andExpect(jsonPath("$.postulacionesPorEstado.ENTREVISTA").value(1))
                .andExpect(jsonPath("$.postulacionesPorEstado.CONTRATADO").value(1))
                .andExpect(jsonPath("$.postulacionesPorEstado.RECHAZADO").value(0))
                .andExpect(jsonPath("$.entrevistasDeHoy").isArray())
                .andExpect(jsonPath("$.entrevistasDeHoy[0].id").value(1));
    }
}
