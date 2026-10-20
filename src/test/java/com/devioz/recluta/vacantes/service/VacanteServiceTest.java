package com.devioz.recluta.vacantes.service;

import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.vacantes.dto.VacanteRequestDTO;
import com.devioz.recluta.vacantes.dto.VacanteResponseDTO;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacanteServiceTest {

    @Mock
    private VacanteRepository vacanteRepository;

    @InjectMocks
    private VacanteServiceImpl vacanteService;

    private Vacante vacanteAbierta;
    private Vacante vacanteCerrada;
    private VacanteRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        vacanteAbierta = Vacante.builder()
                .id(1L)
                .titulo("Backend Developer")
                .cliente("Devioz Tech")
                .seniority(Seniority.SENIOR)
                .salarioMinimo(new BigDecimal("5000.00"))
                .salarioMaximo(new BigDecimal("8000.00"))
                .modalidad(Modalidad.REMOTO)
                .estado(EstadoVacante.ABIERTA)
                .build();

        vacanteCerrada = Vacante.builder()
                .id(2L)
                .titulo("Frontend Developer")
                .cliente("Devioz Tech")
                .seniority(Seniority.JUNIOR)
                .salarioMinimo(new BigDecimal("2500.00"))
                .salarioMaximo(new BigDecimal("3500.00"))
                .modalidad(Modalidad.HIBRIDO)
                .estado(EstadoVacante.CERRADA)
                .build();

        requestValido = new VacanteRequestDTO(
                "Backend Developer",
                "Devioz Tech",
                Seniority.SENIOR,
                new BigDecimal("5000.00"),
                new BigDecimal("8000.00"),
                Modalidad.REMOTO
        );
    }

    @Test
    @DisplayName("Debe crear una vacante correctamente con estado ABIERTA")
    void crearVacanteExitosamente() {
        when(vacanteRepository.save(any(Vacante.class))).thenReturn(vacanteAbierta);

        VacanteResponseDTO resultado = vacanteService.crear(requestValido);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Backend Developer", resultado.titulo());
        assertEquals(EstadoVacante.ABIERTA, resultado.estado());
        verify(vacanteRepository, times(1)).save(any(Vacante.class));
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si salario mínimo es mayor al máximo")
    void crearVacanteConSalarioInvalidoLanzaExcepcion() {
        VacanteRequestDTO requestInvalido = new VacanteRequestDTO(
                "Lead Architect",
                "Devioz Tech",
                Seniority.SENIOR,
                new BigDecimal("12000.00"),
                new BigDecimal("8000.00"),
                Modalidad.REMOTO
        );

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                vacanteService.crear(requestInvalido)
        );

        assertTrue(exception.getMessage().contains("debe ser menor o igual al salario máximo"));
        verify(vacanteRepository, never()).save(any(Vacante.class));
    }

    @Test
    @DisplayName("Debe listar todas las vacantes si no se especifica estado")
    void listarTodasLasVacantes() {
        when(vacanteRepository.findAll()).thenReturn(List.of(vacanteAbierta, vacanteCerrada));

        List<VacanteResponseDTO> resultado = vacanteService.listar(null);

        assertEquals(2, resultado.size());
        verify(vacanteRepository, times(1)).findAll();
        verify(vacanteRepository, never()).findByEstado(any());
    }

    @Test
    @DisplayName("Debe filtrar vacantes por estado cuando se envía el parámetro")
    void listarVacantesPorEstado() {
        when(vacanteRepository.findByEstado(EstadoVacante.ABIERTA)).thenReturn(List.of(vacanteAbierta));

        List<VacanteResponseDTO> resultado = vacanteService.listar(EstadoVacante.ABIERTA);

        assertEquals(1, resultado.size());
        assertEquals(EstadoVacante.ABIERTA, resultado.get(0).estado());
        verify(vacanteRepository, times(1)).findByEstado(EstadoVacante.ABIERTA);
    }

    @Test
    @DisplayName("Debe buscar vacante por ID existente")
    void obtenerPorIdExitoso() {
        when(vacanteRepository.findById(1L)).thenReturn(Optional.of(vacanteAbierta));

        VacanteResponseDTO resultado = vacanteService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la vacante no existe")
    void obtenerPorIdInexistenteLanzaExcepcion() {
        when(vacanteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                vacanteService.obtenerPorId(99L)
        );
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException al intentar editar una vacante CERRADA")
    void actualizarVacanteCerradaLanzaExcepcion() {
        when(vacanteRepository.findById(2L)).thenReturn(Optional.of(vacanteCerrada));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                vacanteService.actualizar(2L, requestValido)
        );

        assertEquals("Una vacante cerrada no se puede editar", exception.getMessage());
        verify(vacanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar correctamente una vacante ABIERTA")
    void actualizarVacanteAbiertaExitoso() {
        when(vacanteRepository.findById(1L)).thenReturn(Optional.of(vacanteAbierta));
        when(vacanteRepository.save(any(Vacante.class))).thenReturn(vacanteAbierta);

        VacanteResponseDTO resultado = vacanteService.actualizar(1L, requestValido);

        assertNotNull(resultado);
        verify(vacanteRepository, times(1)).save(vacanteAbierta);
    }

    @Test
    @DisplayName("Debe cerrar una vacante abierta exitosamente")
    void cerrarVacanteAbiertaExitoso() {
        when(vacanteRepository.findById(1L)).thenReturn(Optional.of(vacanteAbierta));
        when(vacanteRepository.save(any(Vacante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VacanteResponseDTO resultado = vacanteService.cerrar(1L);

        assertNotNull(resultado);
        assertEquals(EstadoVacante.CERRADA, resultado.estado());
        verify(vacanteRepository, times(1)).save(vacanteAbierta);
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException al intentar cerrar una vacante ya CERRADA")
    void cerrarVacanteYaCerradaLanzaExcepcion() {
        when(vacanteRepository.findById(2L)).thenReturn(Optional.of(vacanteCerrada));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                vacanteService.cerrar(2L)
        );

        assertEquals("La vacante ya se encuentra en estado CERRADA", exception.getMessage());
        verify(vacanteRepository, never()).save(any());
    }
}
