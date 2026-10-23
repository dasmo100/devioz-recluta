package com.devioz.recluta.entrevistas.service;

import com.devioz.recluta.entrevistas.dto.EntrevistaCreateDTO;
import com.devioz.recluta.entrevistas.dto.EntrevistaResponseDTO;
import com.devioz.recluta.entrevistas.dto.EntrevistaResultadoDTO;
import com.devioz.recluta.entrevistas.dto.ResumenResponseDTO;
import com.devioz.recluta.entrevistas.entity.Entrevista;
import com.devioz.recluta.entrevistas.entity.EstadoEntrevista;
import com.devioz.recluta.entrevistas.repository.EntrevistaRepository;
import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.postulaciones.entity.EstadoPostulacion;
import com.devioz.recluta.postulaciones.entity.Postulacion;
import com.devioz.recluta.postulaciones.repository.PostulacionRepository;
import com.devioz.recluta.postulantes.entity.Postulante;
import com.devioz.recluta.vacantes.model.EstadoVacante;
import com.devioz.recluta.vacantes.model.Vacante;
import com.devioz.recluta.vacantes.repository.VacanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntrevistaServiceTest {

    @Mock
    private EntrevistaRepository entrevistaRepository;

    @Mock
    private PostulacionRepository postulacionRepository;

    @Mock
    private VacanteRepository vacanteRepository;

    @InjectMocks
    private EntrevistaServiceImpl entrevistaService;

    private Postulacion postulacionEnEntrevista;
    private Postulacion postulacionEnPostulado;
    private Postulante postulante;
    private Vacante vacante;
    private Entrevista entrevistaProgramada;

    @BeforeEach
    void setUp() {
        postulante = Postulante.builder()
                .id(1L)
                .nombres("Carlos")
                .apellidos("Mendoza")
                .dni("45871234")
                .email("carlos.mendoza@gmail.com")
                .build();

        vacante = Vacante.builder()
                .id(1L)
                .titulo("Backend Developer Java")
                .cliente("Devioz Tech")
                .estado(EstadoVacante.ABIERTA)
                .build();

        postulacionEnEntrevista = Postulacion.builder()
                .id(10L)
                .postulante(postulante)
                .vacante(vacante)
                .estado(EstadoPostulacion.ENTREVISTA)
                .fecha(LocalDateTime.now().minusDays(2))
                .build();

        postulacionEnPostulado = Postulacion.builder()
                .id(20L)
                .postulante(postulante)
                .vacante(vacante)
                .estado(EstadoPostulacion.POSTULADO)
                .fecha(LocalDateTime.now().minusDays(1))
                .build();

        entrevistaProgramada = Entrevista.builder()
                .id(100L)
                .postulacion(postulacionEnEntrevista)
                .entrevistador("Ing. Roberto Gómez")
                .fechaHora(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0))
                .duracionMinutos(60)
                .estado(EstadoEntrevista.PROGRAMADA)
                .puntaje(null)
                .comentario("Entrevista técnica inicial")
                .build();
    }

    // 1. Agendar entrevista exitosamente cuando la postulación está en ENTREVISTA y sin cruce de horario
    @Test
    @DisplayName("Debe agendar entrevista exitosamente cuando la postulación está en ENTREVISTA y sin cruce")
    void agendar_Exitoso_CuandoPostulacionEnEntrevistaYSinCruce() {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);
        EntrevistaCreateDTO dto = new EntrevistaCreateDTO(
                "Ing. Roberto Gómez",
                fechaFutura,
                45,
                "Link de Google Meet"
        );

        when(postulacionRepository.findById(10L)).thenReturn(Optional.of(postulacionEnEntrevista));
        when(entrevistaRepository.findByEntrevistadorIgnoreCaseAndEstadoNotAndFechaHoraBetween(
                eq("Ing. Roberto Gómez"), eq(EstadoEntrevista.CANCELADA), any(), any()))
                .thenReturn(Collections.emptyList());

        Entrevista entrevistaGuardada = Entrevista.builder()
                .id(1L)
                .postulacion(postulacionEnEntrevista)
                .entrevistador("Ing. Roberto Gómez")
                .fechaHora(fechaFutura)
                .duracionMinutos(45)
                .estado(EstadoEntrevista.PROGRAMADA)
                .comentario("Link de Google Meet")
                .build();

        when(entrevistaRepository.save(any(Entrevista.class))).thenReturn(entrevistaGuardada);

        EntrevistaResponseDTO response = entrevistaService.agendar(10L, dto);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(10L, response.postulacionId());
        assertEquals("Carlos Mendoza", response.nombrePostulante());
        assertEquals("Backend Developer Java", response.tituloVacante());
        assertEquals("Ing. Roberto Gómez", response.entrevistador());
        assertEquals(EstadoEntrevista.PROGRAMADA, response.estado());
        assertNull(response.puntaje());
        verify(entrevistaRepository).save(any(Entrevista.class));
    }

    // 2. Fallar al agendar si la postulación NO está en estado ENTREVISTA (BusinessRuleException)
    @Test
    @DisplayName("Debe fallar al agendar si la postulación NO está en estado ENTREVISTA")
    void agendar_Falla_CuandoPostulacionNoEstaEnEstadoEntrevista() {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(2);
        EntrevistaCreateDTO dto = new EntrevistaCreateDTO(
                "Ing. Roberto Gómez",
                fechaFutura,
                30,
                null
        );

        when(postulacionRepository.findById(20L)).thenReturn(Optional.of(postulacionEnPostulado));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> entrevistaService.agendar(20L, dto)
        );

        assertTrue(exception.getMessage().contains("Solo se pueden agendar entrevistas para postulaciones en estado ENTREVISTA"));
        verify(entrevistaRepository, never()).save(any());
    }

    // 3. Fallar al agendar en una fecha/hora pasada (BusinessRuleException)
    @Test
    @DisplayName("Debe fallar al agendar si la fecha y hora es anterior a la actual")
    void agendar_Falla_CuandoFechaHoraEnPasado() {
        LocalDateTime fechaPasada = LocalDateTime.now().minusHours(2);
        EntrevistaCreateDTO dto = new EntrevistaCreateDTO(
                "Ing. Roberto Gómez",
                fechaPasada,
                30,
                null
        );

        when(postulacionRepository.findById(10L)).thenReturn(Optional.of(postulacionEnEntrevista));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> entrevistaService.agendar(10L, dto)
        );

        assertTrue(exception.getMessage().contains("No se puede agendar una entrevista en una fecha y hora pasada"));
        verify(entrevistaRepository, never()).save(any());
    }

    // 4. Fallar al agendar cuando el entrevistador tiene cruce de horario con otra entrevista (BusinessRuleException)
    @Test
    @DisplayName("Debe fallar al agendar si existe cruce de horario para el mismo entrevistador")
    void agendar_Falla_CuandoHayCruceDeHorario() {
        LocalDate fecha = LocalDate.now().plusDays(2);
        // Entrevista existente de 10:00 a 11:00
        Entrevista existente = Entrevista.builder()
                .id(200L)
                .entrevistador("Ing. Roberto Gómez")
                .fechaHora(fecha.atTime(10, 0))
                .duracionMinutos(60)
                .estado(EstadoEntrevista.PROGRAMADA)
                .build();

        // Nueva entrevista que se solapa: de 10:30 a 11:30
        LocalDateTime nuevaFechaHora = fecha.atTime(10, 30);
        EntrevistaCreateDTO dto = new EntrevistaCreateDTO(
                "Ing. Roberto Gómez",
                nuevaFechaHora,
                60,
                null
        );

        when(postulacionRepository.findById(10L)).thenReturn(Optional.of(postulacionEnEntrevista));
        when(entrevistaRepository.findByEntrevistadorIgnoreCaseAndEstadoNotAndFechaHoraBetween(
                eq("Ing. Roberto Gómez"), eq(EstadoEntrevista.CANCELADA), any(), any()))
                .thenReturn(List.of(existente));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> entrevistaService.agendar(10L, dto)
        );

        assertTrue(exception.getMessage().contains("El entrevistador ya tiene una entrevista programada en ese rango de horario"));
        verify(entrevistaRepository, never()).save(any());
    }

    // 5. Fallar al marcar entrevista como REALIZADA sin enviar puntaje, y caso exitoso con puntaje de 1 a 5
    @Test
    @DisplayName("Debe fallar al marcar como REALIZADA si no se envía puntaje")
    void registrarResultado_Falla_CuandoEsRealizadaSinPuntaje() {
        when(entrevistaRepository.findById(100L)).thenReturn(Optional.of(entrevistaProgramada));

        EntrevistaResultadoDTO dtoSinPuntaje = new EntrevistaResultadoDTO(
                EstadoEntrevista.REALIZADA,
                null,
                "Buen candidato"
        );

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> entrevistaService.registrarResultado(100L, dtoSinPuntaje)
        );

        assertTrue(exception.getMessage().contains("El puntaje (1 a 5) es obligatorio al marcar la entrevista como REALIZADA"));
        verify(entrevistaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe registrar resultado exitosamente como REALIZADA cuando se provee puntaje válido (1 a 5)")
    void registrarResultado_Exitoso_ConPuntajeValido() {
        when(entrevistaRepository.findById(100L)).thenReturn(Optional.of(entrevistaProgramada));
        when(entrevistaRepository.save(any(Entrevista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EntrevistaResultadoDTO dtoValido = new EntrevistaResultadoDTO(
                EstadoEntrevista.REALIZADA,
                5,
                "Candidato excepcional, domina toda la arquitectura"
        );

        EntrevistaResponseDTO response = entrevistaService.registrarResultado(100L, dtoValido);

        assertNotNull(response);
        assertEquals(EstadoEntrevista.REALIZADA, response.estado());
        assertEquals(5, response.puntaje());
        assertEquals("Candidato excepcional, domina toda la arquitectura", response.comentario());
        verify(entrevistaRepository).save(any(Entrevista.class));
    }

    @Test
    @DisplayName("Debe registrar resultado exitosamente como CANCELADA sin requerir puntaje")
    void registrarResultado_Exitoso_ComoCancelada() {
        when(entrevistaRepository.findById(100L)).thenReturn(Optional.of(entrevistaProgramada));
        when(entrevistaRepository.save(any(Entrevista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EntrevistaResultadoDTO dtoCancelada = new EntrevistaResultadoDTO(
                EstadoEntrevista.CANCELADA,
                null,
                "El candidato no se presentó"
        );

        EntrevistaResponseDTO response = entrevistaService.registrarResultado(100L, dtoCancelada);

        assertNotNull(response);
        assertEquals(EstadoEntrevista.CANCELADA, response.estado());
        assertNull(response.puntaje());
        verify(entrevistaRepository).save(any(Entrevista.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar resultado si la entrevista ya estaba REALIZADA o CANCELADA")
    void registrarResultado_Falla_CuandoYaEstaConcluida() {
        entrevistaProgramada.setEstado(EstadoEntrevista.REALIZADA);
        when(entrevistaRepository.findById(100L)).thenReturn(Optional.of(entrevistaProgramada));

        EntrevistaResultadoDTO dto = new EntrevistaResultadoDTO(
                EstadoEntrevista.CANCELADA,
                null,
                "Intento de cancelar después de realizada"
        );

        assertThrows(BusinessRuleException.class, () -> entrevistaService.registrarResultado(100L, dto));
        verify(entrevistaRepository, never()).save(any());
    }

    // 6. Probar el método de resumen (GET /api/resumen)
    @Test
    @DisplayName("Debe generar el resumen correctamente con vacantes abiertas, conteo de los 5 estados y entrevistas de hoy")
    void obtenerResumen_Exitoso() {
        when(vacanteRepository.countByEstado(EstadoVacante.ABIERTA)).thenReturn(3L);

        when(postulacionRepository.countByEstado(EstadoPostulacion.POSTULADO)).thenReturn(5L);
        when(postulacionRepository.countByEstado(EstadoPostulacion.EN_REVISION)).thenReturn(2L);
        when(postulacionRepository.countByEstado(EstadoPostulacion.ENTREVISTA)).thenReturn(4L);
        when(postulacionRepository.countByEstado(EstadoPostulacion.CONTRATADO)).thenReturn(1L);
        when(postulacionRepository.countByEstado(EstadoPostulacion.RECHAZADO)).thenReturn(0L);

        Entrevista hoy = Entrevista.builder()
                .id(1L)
                .postulacion(postulacionEnEntrevista)
                .entrevistador("Ing. Roberto Gómez")
                .fechaHora(LocalDateTime.now())
                .duracionMinutos(30)
                .estado(EstadoEntrevista.PROGRAMADA)
                .build();

        when(entrevistaRepository.findByFechaHoraBetweenOrderByFechaHoraAsc(any(), any()))
                .thenReturn(List.of(hoy));

        ResumenResponseDTO resumen = entrevistaService.obtenerResumen();

        assertNotNull(resumen);
        assertEquals(3L, resumen.vacantesAbiertas());
        assertEquals(5, resumen.postulacionesPorEstado().size());
        assertEquals(5L, resumen.postulacionesPorEstado().get("POSTULADO"));
        assertEquals(2L, resumen.postulacionesPorEstado().get("EN_REVISION"));
        assertEquals(4L, resumen.postulacionesPorEstado().get("ENTREVISTA"));
        assertEquals(1L, resumen.postulacionesPorEstado().get("CONTRATADO"));
        assertEquals(0L, resumen.postulacionesPorEstado().get("RECHAZADO"));

        assertEquals(1, resumen.entrevistasDeHoy().size());
        assertEquals(1L, resumen.entrevistasDeHoy().get(0).id());
    }

    @Test
    @DisplayName("Debe listar entrevistas filtrando por fecha o devolviendo todas si no se especifica fecha")
    void listarPorFecha() {
        LocalDate fecha = LocalDate.now();
        when(entrevistaRepository.findByFechaHoraBetweenOrderByFechaHoraAsc(any(), any()))
                .thenReturn(List.of(entrevistaProgramada));
        when(entrevistaRepository.findAllByOrderByFechaHoraAsc())
                .thenReturn(List.of(entrevistaProgramada));

        List<EntrevistaResponseDTO> conFecha = entrevistaService.listarPorFecha(fecha);
        List<EntrevistaResponseDTO> sinFecha = entrevistaService.listarPorFecha(null);

        assertEquals(1, conFecha.size());
        assertEquals(1, sinFecha.size());
        verify(entrevistaRepository).findByFechaHoraBetweenOrderByFechaHoraAsc(any(), any());
        verify(entrevistaRepository).findAllByOrderByFechaHoraAsc();
    }
}
