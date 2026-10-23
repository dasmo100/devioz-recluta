package com.devioz.recluta.postulantes.controller;

import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.GlobalExceptionHandler;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.postulantes.dto.PostulanteRequestDTO;
import com.devioz.recluta.postulantes.dto.PostulanteResponseDTO;
import com.devioz.recluta.postulantes.service.PostulanteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostulanteController.class)
@Import(GlobalExceptionHandler.class)
class PostulanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostulanteService postulanteService;

    private PostulanteResponseDTO responseDTO;
    private PostulanteRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new PostulanteResponseDTO(
                1L,
                "Juan Carlos",
                "Pérez Gómez",
                "71234567",
                "juan.perez@example.com",
                "987654321",
                5,
                "Java 21, Spring Boot, MySQL",
                LocalDate.of(2026, 10, 23)
        );

        requestDTO = new PostulanteRequestDTO(
                "Juan Carlos",
                "Pérez Gómez",
                "71234567",
                "juan.perez@example.com",
                "987654321",
                5,
                "Java 21, Spring Boot, MySQL"
        );
    }

    @Test
    @DisplayName("GET /api/postulantes debe retornar 200 y la página de postulantes")
    void buscarPostulantesDebeRetornar200() throws Exception {
        when(postulanteService.buscar(any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(responseDTO)));

        mockMvc.perform(get("/api/postulantes")
                        .param("q", "Juan")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].dni").value("71234567"))
                .andExpect(jsonPath("$.content[0].email").value("juan.perez@example.com"));
    }

    @Test
    @DisplayName("GET /api/postulantes/{id} debe retornar 200 cuando existe")
    void obtenerPorIdDebeRetornar200() throws Exception {
        when(postulanteService.obtenerPorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/postulantes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombres").value("Juan Carlos"))
                .andExpect(jsonPath("$.dni").value("71234567"));
    }

    @Test
    @DisplayName("GET /api/postulantes/{id} debe retornar 404 cuando no existe")
    void obtenerPorIdInexistenteDebeRetornar404() throws Exception {
        when(postulanteService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Postulante no encontrado con ID: 99"));

        mockMvc.perform(get("/api/postulantes/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Postulante no encontrado con ID: 99"));
    }

    @Test
    @DisplayName("POST /api/postulantes debe retornar 201 al crear exitosamente")
    void crearPostulanteDebeRetornar201() throws Exception {
        when(postulanteService.crear(any(PostulanteRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/postulantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dni").value("71234567"));
    }

    @Test
    @DisplayName("POST /api/postulantes debe retornar 400 cuando validaciones de campos fallan")
    void crearPostulanteInvalidoDebeRetornar400() throws Exception {
        PostulanteRequestDTO requestInvalido = new PostulanteRequestDTO(
                "",                 // nombres vacíos
                "",                 // apellidos vacíos
                "123",              // DNI no tiene 8 dígitos
                "correo-invalido",  // email no válido
                "987654321",
                -1,                 // anios de experiencia negativo
                ""                  // habilidades vacías
        );

        mockMvc.perform(post("/api/postulantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /api/postulantes debe retornar 400 si DNI o email están duplicados")
    void crearPostulanteDniDuplicadoDebeRetornar400() throws Exception {
        when(postulanteService.crear(any(PostulanteRequestDTO.class)))
                .thenThrow(new BusinessRuleException("Ya existe un postulante registrado con el DNI 71234567"));

        mockMvc.perform(post("/api/postulantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Ya existe un postulante registrado con el DNI 71234567"));
    }

    @Test
    @DisplayName("PUT /api/postulantes/{id} debe retornar 200 al actualizar")
    void actualizarPostulanteDebeRetornar200() throws Exception {
        when(postulanteService.actualizar(eq(1L), any(PostulanteRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/postulantes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/postulantes/{id} debe retornar 204 al eliminar")
    void eliminarPostulanteDebeRetornar204() throws Exception {
        doNothing().when(postulanteService).eliminar(1L);

        mockMvc.perform(delete("/api/postulantes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/postulantes/{id} debe retornar 400 si tiene postulaciones")
    void eliminarPostulanteConPostulacionesDebeRetornar400() throws Exception {
        doThrow(new BusinessRuleException("No se puede eliminar al postulante porque tiene postulaciones registradas"))
                .when(postulanteService).eliminar(1L);

        mockMvc.perform(delete("/api/postulantes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("No se puede eliminar al postulante porque tiene postulaciones registradas"));
    }
}
