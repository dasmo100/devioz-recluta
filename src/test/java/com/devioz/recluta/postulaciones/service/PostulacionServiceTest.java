package com.devioz.recluta.postulaciones.service;

import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.postulaciones.dto.CambioEstadoDTO;
import com.devioz.recluta.postulaciones.dto.HistorialEstadoResponseDTO;
import com.devioz.recluta.postulaciones.dto.PostulacionCreateDTO;
import com.devioz.recluta.postulaciones.dto.PostulacionResponseDTO;
import com.devioz.recluta.postulaciones.entity.EstadoPostulacion;
import com.devioz.recluta.postulaciones.entity.HistorialEstado;
import com.devioz.recluta.postulaciones.entity.Postulacion;
import com.devioz.recluta.postulaciones.repository.HistorialEstadoRepository;
import com.devioz.recluta.postulaciones.repository.PostulacionRepository;
import com.devioz.recluta.postulantes.entity.Postulante;
import com.devioz.recluta.postulantes.repository.PostulanteRepository;
import com.devioz.recluta.vacantes.model.EstadoVacante;
import com.devioz.recluta.vacantes.model.Modalidad;
import com.devioz.recluta.vacantes.model.Seniority;
import com.devioz.recluta.vacantes.model.Vacante;
import com.devioz.recluta.vacantes.repository.VacanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostulacionServiceTest {

    @Mock
    private PostulacionRepository postulacionRepository;

    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    @Mock
    private VacanteRepository vacanteRepository;

    @Mock
    private PostulanteRepository postulanteRepository;

    @InjectMocks
    private PostulacionServiceImpl postulacionService;

    private Vacante vacanteAbierta;
    private Vacante vacanteCerrada;
    private Postulante postulante;
    private Postulacion postulacion;

    @BeforeEach
    void setUp() {
        vacanteAbierta = Vacante.builder()
                .id(1L)
                .titulo("Backend Developer Java")
                .cliente("Devioz Tech")
                .seniority(Seniority.SENIOR)
                .salarioMinimo(new BigDecimal("5000.00"))
                .salarioMaximo(new BigDecimal("8000.00"))
                .modalidad(Modalidad.REMOTO)
                .estado(EstadoVacante.ABIERTA)
                .build();

        vacanteCerrada = Vacante.builder()
                .id(2L)
                .titulo("Frontend Developer React")
                .cliente("Devioz Tech")
                .seniority(Seniority.SEMI_SENIOR)
                .salarioMinimo(new BigDecimal("4000.00"))
                .salarioMaximo(new BigDecimal("6000.00"))
                .modalidad(Modalidad.HIBRIDO)
                .estado(EstadoVacante.CERRADA)
                .build();

        postulante = Postulante.builder()
                .id(10L)
                .nombres("Carlos")
                .apellidos("Gómez Silva")
                .dni("12345678")
                .email("carlos.gomez@example.com")
                .telefono("987654321")
                .aniosExperiencia(4)
                .habilidades("Java, Spring Boot")
                .fechaRegistro(LocalDate.now())
                .build();

        postulacion = Postulacion.builder()
                .id(100L)
                .vacante(vacanteAbierta)
                .postulante(postulante)
                .estado(EstadoPostulacion.POSTULADO)
                .fecha(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("1. Postular exitosamente a una vacante abierta y registrar en el historial")
    void postularExitosamente() {
        PostulacionCreateDTO dto = new PostulacionCreateDTO(10L, "Postulando desde portal de empleo");

        when(vacanteRepository.findById(1L)).thenReturn(Optional.of(vacanteAbierta));
        when(postulanteRepository.findById(10L)).thenReturn(Optional.of(postulante));
        when(postulacionRepository.existsByPostulanteIdAndVacanteId(10L, 1L)).thenReturn(false);
        when(postulacionRepository.save(any(Postulacion.class))).thenReturn(postulacion);
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(i -> i.getArgument(0));

        PostulacionResponseDTO response = postulacionService.crear(1L, dto);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(10L, response.postulanteId());
        assertEquals(1L, response.vacanteId());
        assertEquals(EstadoPostulacion.POSTULADO, response.estado());
        assertEquals("Carlos Gómez Silva", response.nombreCompletoPostulante());
        assertEquals("12345678", response.dniPostulante());

        verify(postulacionRepository, times(1)).save(any(Postulacion.class));
        verify(historialEstadoRepository, times(1)).save(any(HistorialEstado.class));
    }

    @Test
    @DisplayName("2. Fallar al postular a una vacante CERRADA (BusinessRuleException)")
    void postularVacanteCerradaLanzaExcepcion() {
        PostulacionCreateDTO dto = new PostulacionCreateDTO(10L, "Intento en vacante cerrada");

        when(vacanteRepository.findById(2L)).thenReturn(Optional.of(vacanteCerrada));
        when(postulanteRepository.findById(10L)).thenReturn(Optional.of(postulante));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulacionService.crear(2L, dto)
        );

        assertTrue(exception.getMessage().contains("cerrada"));
        verify(postulacionRepository, never()).save(any(Postulacion.class));
        verify(historialEstadoRepository, never()).save(any(HistorialEstado.class));
    }

    @Test
    @DisplayName("3. Fallar al postular dos veces a la misma vacante (BusinessRuleException)")
    void postularDuplicadoLanzaExcepcion() {
        PostulacionCreateDTO dto = new PostulacionCreateDTO(10L, "Intento duplicado");

        when(vacanteRepository.findById(1L)).thenReturn(Optional.of(vacanteAbierta));
        when(postulanteRepository.findById(10L)).thenReturn(Optional.of(postulante));
        when(postulacionRepository.existsByPostulanteIdAndVacanteId(10L, 1L)).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulacionService.crear(1L, dto)
        );

        assertTrue(exception.getMessage().contains("ya tiene una postulación registrada"));
        verify(postulacionRepository, never()).save(any(Postulacion.class));
        verify(historialEstadoRepository, never()).save(any(HistorialEstado.class));
    }

    @Test
    @DisplayName("4. Fallar al intentar saltar etapas (ej. POSTULADO a ENTREVISTA, POSTULADO a CONTRATADO o desde RECHAZADO)")
    void saltarEtapasLanzaExcepcion() {
        // Caso A: De POSTULADO directo a ENTREVISTA
        postulacion.setEstado(EstadoPostulacion.POSTULADO);
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));

        CambioEstadoDTO dtoEntrevista = new CambioEstadoDTO(EstadoPostulacion.ENTREVISTA, "Salto a entrevista");
        BusinessRuleException ex1 = assertThrows(BusinessRuleException.class, () ->
                postulacionService.cambiarEstado(100L, dtoEntrevista)
        );
        assertTrue(ex1.getMessage().contains("Transición no permitida"));

        // Caso B: De POSTULADO directo a CONTRATADO
        CambioEstadoDTO dtoContratado = new CambioEstadoDTO(EstadoPostulacion.CONTRATADO, "Salto a contratado");
        BusinessRuleException ex2 = assertThrows(BusinessRuleException.class, () ->
                postulacionService.cambiarEstado(100L, dtoContratado)
        );
        assertTrue(ex2.getMessage().contains("Transición no permitida"));

        // Caso C: Cambiar estado desde un estado final RECHAZADO
        postulacion.setEstado(EstadoPostulacion.RECHAZADO);
        CambioEstadoDTO dtoRechazado = new CambioEstadoDTO(EstadoPostulacion.EN_REVISION, "Intentar revivir postulación");
        BusinessRuleException ex3 = assertThrows(BusinessRuleException.class, () ->
                postulacionService.cambiarEstado(100L, dtoRechazado)
        );
        assertTrue(ex3.getMessage().contains("estado final"));

        verify(postulacionRepository, never()).save(any());
        verify(historialEstadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("5. Pasar exitosamente de ENTREVISTA a CONTRATADO, verificando que la vacante cambie a CERRADA y se guarde historial")
    void pasarEntrevistaAContratadoCierraVacanteYGuardaHistorial() {
        postulacion.setEstado(EstadoPostulacion.ENTREVISTA);
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));
        when(postulacionRepository.save(any(Postulacion.class))).thenReturn(postulacion);
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(i -> i.getArgument(0));
        when(vacanteRepository.save(any(Vacante.class))).thenReturn(vacanteAbierta);

        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoPostulacion.CONTRATADO, "Excelente desempeño en pruebas técnicas y culturales");

        PostulacionResponseDTO response = postulacionService.cambiarEstado(100L, dto);

        assertNotNull(response);
        assertEquals(EstadoPostulacion.CONTRATADO, postulacion.getEstado());
        assertEquals(EstadoVacante.CERRADA, vacanteAbierta.getEstado());

        verify(vacanteRepository, times(1)).save(vacanteAbierta);
        verify(historialEstadoRepository, times(1)).save(any(HistorialEstado.class));
        verify(postulacionRepository, times(1)).save(postulacion);
    }

    @Test
    @DisplayName("6. Transición exitosa de POSTULADO a EN_REVISION")
    void transicionPostuladoAEnRevisionExitoso() {
        postulacion.setEstado(EstadoPostulacion.POSTULADO);
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));
        when(postulacionRepository.save(any(Postulacion.class))).thenReturn(postulacion);
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(i -> i.getArgument(0));

        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoPostulacion.EN_REVISION, "Iniciando revisión curricular");

        PostulacionResponseDTO response = postulacionService.cambiarEstado(100L, dto);

        assertNotNull(response);
        assertEquals(EstadoPostulacion.EN_REVISION, postulacion.getEstado());
        verify(historialEstadoRepository, times(1)).save(any(HistorialEstado.class));
        verify(vacanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("7. Transición exitosa de EN_REVISION a ENTREVISTA")
    void transicionEnRevisionAEntrevistaExitoso() {
        postulacion.setEstado(EstadoPostulacion.EN_REVISION);
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));
        when(postulacionRepository.save(any(Postulacion.class))).thenReturn(postulacion);
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(i -> i.getArgument(0));

        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoPostulacion.ENTREVISTA, "Candidato seleccionado para entrevista con líder técnico");

        PostulacionResponseDTO response = postulacionService.cambiarEstado(100L, dto);

        assertNotNull(response);
        assertEquals(EstadoPostulacion.ENTREVISTA, postulacion.getEstado());
        verify(historialEstadoRepository, times(1)).save(any(HistorialEstado.class));
    }

    @Test
    @DisplayName("8. Transición exitosa a RECHAZADO desde EN_REVISION")
    void transicionARechazadoExitoso() {
        postulacion.setEstado(EstadoPostulacion.EN_REVISION);
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));
        when(postulacionRepository.save(any(Postulacion.class))).thenReturn(postulacion);
        when(historialEstadoRepository.save(any(HistorialEstado.class))).thenAnswer(i -> i.getArgument(0));

        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoPostulacion.RECHAZADO, "No cumple con los años mínimos requeridos");

        PostulacionResponseDTO response = postulacionService.cambiarEstado(100L, dto);

        assertNotNull(response);
        assertEquals(EstadoPostulacion.RECHAZADO, postulacion.getEstado());
        verify(historialEstadoRepository, times(1)).save(any(HistorialEstado.class));
    }

    @Test
    @DisplayName("9. Fallar al cambiar estado si ya está CONTRATADO")
    void cambiarEstadoDesdeContratadoLanzaExcepcion() {
        postulacion.setEstado(EstadoPostulacion.CONTRATADO);
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));

        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoPostulacion.RECHAZADO, "Intento inválido");

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulacionService.cambiarEstado(100L, dto)
        );

        assertTrue(exception.getMessage().contains("estado final"));
    }

    @Test
    @DisplayName("10. Listar postulaciones de una vacante exitosamente")
    void listarPorVacanteExitoso() {
        when(vacanteRepository.existsById(1L)).thenReturn(true);
        when(postulacionRepository.findByVacanteId(1L)).thenReturn(List.of(postulacion));

        List<PostulacionResponseDTO> lista = postulacionService.listarPorVacante(1L);

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(100L, lista.get(0).id());
        verify(postulacionRepository, times(1)).findByVacanteId(1L);
    }

    @Test
    @DisplayName("11. Fallar al listar postulaciones si la vacante no existe")
    void listarPorVacanteInexistenteLanzaExcepcion() {
        when(vacanteRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                postulacionService.listarPorVacante(99L)
        );
    }

    @Test
    @DisplayName("12. Consultar historial de estados cronológico exitosamente")
    void consultarHistorialExitoso() {
        HistorialEstado h1 = HistorialEstado.builder()
                .id(1L)
                .postulacion(postulacion)
                .estadoAnterior(null)
                .estadoNuevo(EstadoPostulacion.POSTULADO)
                .fecha(LocalDateTime.now().minusHours(2))
                .comentario("Registro inicial")
                .build();

        HistorialEstado h2 = HistorialEstado.builder()
                .id(2L)
                .postulacion(postulacion)
                .estadoAnterior(EstadoPostulacion.POSTULADO)
                .estadoNuevo(EstadoPostulacion.EN_REVISION)
                .fecha(LocalDateTime.now().minusHours(1))
                .comentario("En revisión")
                .build();

        when(postulacionRepository.existsById(100L)).thenReturn(true);
        when(historialEstadoRepository.findByPostulacionIdOrderByFechaAsc(100L)).thenReturn(List.of(h1, h2));

        List<HistorialEstadoResponseDTO> historial = postulacionService.consultarHistorial(100L);

        assertNotNull(historial);
        assertEquals(2, historial.size());
        assertEquals(EstadoPostulacion.POSTULADO, historial.get(0).estadoNuevo());
        assertEquals(EstadoPostulacion.EN_REVISION, historial.get(1).estadoNuevo());
        verify(historialEstadoRepository, times(1)).findByPostulacionIdOrderByFechaAsc(100L);
    }

    @Test
    @DisplayName("13. Fallar al consultar historial si la postulación no existe")
    void consultarHistorialInexistenteLanzaExcepcion() {
        when(postulacionRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                postulacionService.consultarHistorial(999L)
        );
    }
}
