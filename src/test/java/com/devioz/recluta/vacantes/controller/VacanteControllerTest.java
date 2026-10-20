package com.devioz.recluta.vacantes.controller;

import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.GlobalExceptionHandler;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.vacantes.dto.VacanteRequestDTO;
import com.devioz.recluta.vacantes.dto.VacanteResponseDTO;
import com.devioz.recluta.vacantes.model.EstadoVacante;
import com.devioz.recluta.vacantes.model.Modalidad;
import com.devioz.recluta.vacantes.model.Seniority;
import com.devioz.recluta.vacantes.service.VacanteService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VacanteController.class)
@Import(GlobalExceptionHandler.class)
class VacanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VacanteService vacanteService;

    private VacanteResponseDTO responseDTO;
    private VacanteRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new VacanteResponseDTO(
                1L,
                "Desarrollador Java Senior",
                "Banco de Crédito",
                Seniority.SENIOR,
                new BigDecimal("6000.00"),
                new BigDecimal("9000.00"),
                Modalidad.REMOTO,
                EstadoVacante.ABIERTA
        );

        requestDTO = new VacanteRequestDTO(
                "Desarrollador Java Senior",
                "Banco de Crédito",
                Seniority.SENIOR,
                new BigDecimal("6000.00"),
                new BigDecimal("9000.00"),
                Modalidad.REMOTO
        );
    }

    @Test
    @DisplayName("GET /api/vacantes debe retornar status 200 y listado de vacantes")
    void listarVacantesDebeRetornar200() throws Exception {
        when(vacanteService.listar(null)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/vacantes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].titulo").value("Desarrollador Java Senior"))
                .andExpect(jsonPath("$[0].estado").value("ABIERTA"));
    }

    @Test
    @DisplayName("GET /api/vacantes/{id} debe retornar 200 cuando existe")
    void obtenerPorIdDebeRetornar200() throws Exception {
        when(vacanteService.obtenerPorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/vacantes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Desarrollador Java Senior"));
    }

    @Test
    @DisplayName("GET /api/vacantes/{id} debe retornar 404 cuando no existe")
    void obtenerPorIdInexistenteDebeRetornar404() throws Exception {
        when(vacanteService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Vacante", "id", 99L));

        mockMvc.perform(get("/api/vacantes/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/vacantes debe retornar 201 al crear exitosamente")
    void crearVacanteDebeRetornar201() throws Exception {
        when(vacanteService.crear(any(VacanteRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/vacantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Desarrollador Java Senior"));
    }

    @Test
    @DisplayName("POST /api/vacantes debe retornar 400 cuando los campos son inválidos")
    void crearVacanteInvalidaDebeRetornar400() throws Exception {
        VacanteRequestDTO requestInvalido = new VacanteRequestDTO(
                "", // Titulo vacío
                "Cliente X",
                Seniority.JUNIOR,
                new BigDecimal("5000.00"),
                new BigDecimal("2000.00"), // Mínimo mayor que máximo
                Modalidad.REMOTO
        );

        mockMvc.perform(post("/api/vacantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("PUT /api/vacantes/{id} debe retornar 200 al actualizar")
    void actualizarVacanteDebeRetornar200() throws Exception {
        when(vacanteService.actualizar(eq(1L), any(VacanteRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/vacantes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("PUT /api/vacantes/{id} debe retornar 400 si se intenta editar una vacante CERRADA")
    void actualizarVacanteCerradaDebeRetornar400() throws Exception {
        when(vacanteService.actualizar(eq(2L), any(VacanteRequestDTO.class)))
                .thenThrow(new BusinessRuleException("Una vacante cerrada no se puede editar"));

        mockMvc.perform(put("/api/vacantes/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Una vacante cerrada no se puede editar"));
    }

    @Test
    @DisplayName("PATCH /api/vacantes/{id}/cerrar debe retornar 200")
    void cerrarVacanteDebeRetornar200() throws Exception {
        VacanteResponseDTO responseCerrada = new VacanteResponseDTO(
                1L,
                "Desarrollador Java Senior",
                "Banco de Crédito",
                Seniority.SENIOR,
                new BigDecimal("6000.00"),
                new BigDecimal("9000.00"),
                Modalidad.REMOTO,
                EstadoVacante.CERRADA
        );
        when(vacanteService.cerrar(1L)).thenReturn(responseCerrada);

        mockMvc.perform(patch("/api/vacantes/1/cerrar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"));
    }
}
