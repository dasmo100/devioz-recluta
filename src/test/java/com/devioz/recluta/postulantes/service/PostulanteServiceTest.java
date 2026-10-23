package com.devioz.recluta.postulantes.service;

import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.postulaciones.repository.PostulacionRepository;
import com.devioz.recluta.postulantes.dto.PostulanteRequestDTO;
import com.devioz.recluta.postulantes.dto.PostulanteResponseDTO;
import com.devioz.recluta.postulantes.entity.Postulante;
import com.devioz.recluta.postulantes.repository.PostulanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostulanteServiceTest {

    @Mock
    private PostulanteRepository postulanteRepository;

    @Mock
    private PostulacionRepository postulacionRepository;

    @InjectMocks
    private PostulanteServiceImpl postulanteService;

    private Postulante postulanteExistente;
    private PostulanteRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        postulanteExistente = Postulante.builder()
                .id(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez Gómez")
                .dni("71234567")
                .email("juan.perez@example.com")
                .telefono("987654321")
                .aniosExperiencia(5)
                .habilidades("Java 21, Spring Boot, MySQL")
                .fechaRegistro(LocalDate.of(2026, 10, 1))
                .build();

        requestValido = new PostulanteRequestDTO(
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
    @DisplayName("1. Crear postulante exitosamente cuando DNI y email son únicos")
    void crearPostulanteExitosamente() {
        when(postulanteRepository.existsByDni("71234567")).thenReturn(false);
        when(postulanteRepository.existsByEmail("juan.perez@example.com")).thenReturn(false);
        when(postulanteRepository.save(any(Postulante.class))).thenReturn(postulanteExistente);

        PostulanteResponseDTO resultado = postulanteService.crear(requestValido);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Juan Carlos", resultado.nombres());
        assertEquals("Pérez Gómez", resultado.apellidos());
        assertEquals("71234567", resultado.dni());
        assertEquals("juan.perez@example.com", resultado.email());
        assertEquals(5, resultado.aniosExperiencia());
        assertEquals("Java 21, Spring Boot, MySQL", resultado.habilidades());
        verify(postulanteRepository, times(1)).save(any(Postulante.class));
    }

    @Test
    @DisplayName("2. Fallar al crear postulante con DNI duplicado (debe lanzar BusinessRuleException)")
    void crearPostulanteConDniDuplicadoLanzaExcepcion() {
        when(postulanteRepository.existsByDni("71234567")).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulanteService.crear(requestValido)
        );

        assertTrue(exception.getMessage().contains("Ya existe un postulante registrado con el DNI 71234567"));
        verify(postulanteRepository, never()).save(any(Postulante.class));
    }

    @Test
    @DisplayName("3. Fallar al buscar un postulante por ID que no existe (debe lanzar ResourceNotFoundException)")
    void obtenerPorIdInexistenteLanzaExcepcion() {
        when(postulanteRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                postulanteService.obtenerPorId(99L)
        );

        assertEquals("Postulante no encontrado con ID: 99", exception.getMessage());
        verify(postulanteRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("4. Fallar al intentar eliminar un postulante que tiene postulaciones asociadas (debe lanzar BusinessRuleException)")
    void eliminarPostulanteConPostulacionesLanzaExcepcion() {
        when(postulanteRepository.findById(1L)).thenReturn(Optional.of(postulanteExistente));
        when(postulacionRepository.existsByPostulanteId(1L)).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulanteService.eliminar(1L)
        );

        assertEquals("No se puede eliminar al postulante porque tiene postulaciones registradas", exception.getMessage());
        verify(postulanteRepository, never()).delete(any(Postulante.class));
    }

    @Test
    @DisplayName("5. Fallar al crear postulante con Email duplicado (debe lanzar BusinessRuleException)")
    void crearPostulanteConEmailDuplicadoLanzaExcepcion() {
        when(postulanteRepository.existsByDni("71234567")).thenReturn(false);
        when(postulanteRepository.existsByEmail("juan.perez@example.com")).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulanteService.crear(requestValido)
        );

        assertTrue(exception.getMessage().contains("Ya existe un postulante con el email juan.perez@example.com"));
        verify(postulanteRepository, never()).save(any(Postulante.class));
    }

    @Test
    @DisplayName("6. Obtener postulante por ID existente con éxito")
    void obtenerPorIdExitoso() {
        when(postulanteRepository.findById(1L)).thenReturn(Optional.of(postulanteExistente));

        PostulanteResponseDTO resultado = postulanteService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("71234567", resultado.dni());
        verify(postulanteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("7. Actualizar postulante exitosamente")
    void actualizarPostulanteExitoso() {
        PostulanteRequestDTO updateRequest = new PostulanteRequestDTO(
                "Juan C.",
                "Pérez Actualizado",
                "71234567",
                "juan.nuevo@example.com",
                "999888777",
                6,
                "Java 21, Spring Boot 3, AWS"
        );

        when(postulanteRepository.findById(1L)).thenReturn(Optional.of(postulanteExistente));
        when(postulanteRepository.existsByDniAndIdNot("71234567", 1L)).thenReturn(false);
        when(postulanteRepository.existsByEmailAndIdNot("juan.nuevo@example.com", 1L)).thenReturn(false);
        when(postulanteRepository.save(any(Postulante.class))).thenReturn(postulanteExistente);

        PostulanteResponseDTO resultado = postulanteService.actualizar(1L, updateRequest);

        assertNotNull(resultado);
        verify(postulanteRepository, times(1)).save(postulanteExistente);
    }

    @Test
    @DisplayName("8. Fallar al actualizar con DNI perteneciente a otro postulante")
    void actualizarConDniDeOtroLanzaExcepcion() {
        PostulanteRequestDTO updateRequest = new PostulanteRequestDTO(
                "Juan Carlos",
                "Pérez",
                "87654321",
                "juan@example.com",
                "987654321",
                5,
                "Java"
        );

        when(postulanteRepository.findById(1L)).thenReturn(Optional.of(postulanteExistente));
        when(postulanteRepository.existsByDniAndIdNot("87654321", 1L)).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulanteService.actualizar(1L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Ya existe un postulante registrado con el DNI 87654321"));
        verify(postulanteRepository, never()).save(any(Postulante.class));
    }

    @Test
    @DisplayName("9. Fallar al actualizar con Email perteneciente a otro postulante")
    void actualizarConEmailDeOtroLanzaExcepcion() {
        PostulanteRequestDTO updateRequest = new PostulanteRequestDTO(
                "Juan Carlos",
                "Pérez",
                "71234567",
                "otro@example.com",
                "987654321",
                5,
                "Java"
        );

        when(postulanteRepository.findById(1L)).thenReturn(Optional.of(postulanteExistente));
        when(postulanteRepository.existsByDniAndIdNot("71234567", 1L)).thenReturn(false);
        when(postulanteRepository.existsByEmailAndIdNot("otro@example.com", 1L)).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulanteService.actualizar(1L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Ya existe un postulante con el email otro@example.com"));
        verify(postulanteRepository, never()).save(any(Postulante.class));
    }

    @Test
    @DisplayName("10. Buscar postulantes con paginación y filtro de texto")
    void buscarConFiltroExitoso() {
        Page<Postulante> pageMock = new PageImpl<>(List.of(postulanteExistente));
        when(postulanteRepository.buscar(eq("Juan"), any(Pageable.class))).thenReturn(pageMock);

        Page<PostulanteResponseDTO> resultado = postulanteService.buscar("Juan", 0, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Juan Carlos", resultado.getContent().get(0).nombres());
        verify(postulanteRepository, times(1)).buscar(eq("Juan"), any(Pageable.class));
    }

    @Test
    @DisplayName("11. Buscar postulantes cuando q es nulo o vacío devuelve todos paginados")
    void buscarSinFiltroExitoso() {
        Page<Postulante> pageMock = new PageImpl<>(List.of(postulanteExistente));
        when(postulanteRepository.buscar(isNull(), any(Pageable.class))).thenReturn(pageMock);

        Page<PostulanteResponseDTO> resultado = postulanteService.buscar(null, 0, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(postulanteRepository, times(1)).buscar(isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("12. Eliminar postulante exitosamente sin postulaciones asociadas")
    void eliminarPostulanteExitoso() {
        when(postulanteRepository.findById(1L)).thenReturn(Optional.of(postulanteExistente));
        when(postulacionRepository.existsByPostulanteId(1L)).thenReturn(false);
        doNothing().when(postulanteRepository).delete(postulanteExistente);

        assertDoesNotThrow(() -> postulanteService.eliminar(1L));
        verify(postulanteRepository, times(1)).delete(postulanteExistente);
    }

    @Test
    @DisplayName("13. Fallar al eliminar si la BD lanza DataIntegrityViolationException")
    void eliminarConRestriccionFKLanzaBusinessRuleException() {
        when(postulanteRepository.findById(1L)).thenReturn(Optional.of(postulanteExistente));
        when(postulacionRepository.existsByPostulanteId(1L)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(postulanteRepository).delete(postulanteExistente);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                postulanteService.eliminar(1L)
        );

        assertEquals("No se puede eliminar al postulante porque tiene postulaciones registradas", exception.getMessage());
    }

    @Test
    @DisplayName("14. Fallar al eliminar un postulante que no existe")
    void eliminarInexistenteLanzaResourceNotFoundException() {
        when(postulanteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                postulanteService.eliminar(99L)
        );
        verify(postulanteRepository, never()).delete(any(Postulante.class));
    }
}
