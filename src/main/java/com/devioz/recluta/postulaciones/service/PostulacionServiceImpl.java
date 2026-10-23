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
import com.devioz.recluta.vacantes.model.Vacante;
import com.devioz.recluta.vacantes.repository.VacanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostulacionServiceImpl implements PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final VacanteRepository vacanteRepository;
    private final PostulanteRepository postulanteRepository;

    @Override
    public PostulacionResponseDTO crear(Long vacanteId, PostulacionCreateDTO dto) {
        Vacante vacante = vacanteRepository.findById(vacanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacante no encontrada con ID: " + vacanteId));

        Postulante postulante = postulanteRepository.findById(dto.postulanteId())
                .orElseThrow(() -> new ResourceNotFoundException("Postulante no encontrado con ID: " + dto.postulanteId()));

        if (vacante.getEstado() == EstadoVacante.CERRADA) {
            throw new BusinessRuleException("No se puede postular a una vacante que se encuentra cerrada");
        }

        if (postulacionRepository.existsByPostulanteIdAndVacanteId(dto.postulanteId(), vacanteId)) {
            throw new BusinessRuleException("El postulante ya tiene una postulación registrada para esta vacante");
        }

        LocalDateTime ahora = LocalDateTime.now();

        Postulacion postulacion = Postulacion.builder()
                .vacante(vacante)
                .postulante(postulante)
                .estado(EstadoPostulacion.POSTULADO)
                .fecha(ahora)
                .build();

        Postulacion guardada = postulacionRepository.save(postulacion);

        String comentarioInicial = (dto.comentario() != null && !dto.comentario().trim().isEmpty())
                ? dto.comentario().trim()
                : "Registro inicial de la postulación";

        HistorialEstado historial = HistorialEstado.builder()
                .postulacion(guardada)
                .estadoAnterior(null)
                .estadoNuevo(EstadoPostulacion.POSTULADO)
                .fecha(ahora)
                .comentario(comentarioInicial)
                .build();

        historialEstadoRepository.save(historial);

        return PostulacionResponseDTO.fromEntity(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPorVacante(Long vacanteId) {
        if (!vacanteRepository.existsById(vacanteId)) {
            throw new ResourceNotFoundException("Vacante no encontrada con ID: " + vacanteId);
        }

        List<Postulacion> postulaciones = postulacionRepository.findByVacanteId(vacanteId);
        return postulaciones.stream()
                .map(PostulacionResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public PostulacionResponseDTO cambiarEstado(Long postulacionId, CambioEstadoDTO dto) {
        Postulacion postulacion = postulacionRepository.findById(postulacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Postulación no encontrada con ID: " + postulacionId));

        EstadoPostulacion actual = postulacion.getEstado();
        EstadoPostulacion nuevo = dto.nuevoEstado();

        validarTransicionEstado(actual, nuevo);

        if (nuevo == EstadoPostulacion.CONTRATADO) {
            Vacante vacante = postulacion.getVacante();
            if (vacante != null) {
                vacante.setEstado(EstadoVacante.CERRADA);
                vacanteRepository.save(vacante);
            }
        }

        HistorialEstado historial = HistorialEstado.builder()
                .postulacion(postulacion)
                .estadoAnterior(actual)
                .estadoNuevo(nuevo)
                .fecha(LocalDateTime.now())
                .comentario(dto.comentario().trim())
                .build();

        historialEstadoRepository.save(historial);

        postulacion.setEstado(nuevo);
        Postulacion actualizada = postulacionRepository.save(postulacion);

        return PostulacionResponseDTO.fromEntity(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoResponseDTO> consultarHistorial(Long postulacionId) {
        if (!postulacionRepository.existsById(postulacionId)) {
            throw new ResourceNotFoundException("Postulación no encontrada con ID: " + postulacionId);
        }

        List<HistorialEstado> historial = historialEstadoRepository.findByPostulacionIdOrderByFechaAsc(postulacionId);
        return historial.stream()
                .map(HistorialEstadoResponseDTO::fromEntity)
                .toList();
    }

    private void validarTransicionEstado(EstadoPostulacion actual, EstadoPostulacion nuevo) {
        if (actual == EstadoPostulacion.CONTRATADO || actual == EstadoPostulacion.RECHAZADO) {
            throw new BusinessRuleException(String.format(
                    "No se puede cambiar el estado de una postulación en estado final '%s'", actual));
        }

        if (actual == nuevo) {
            throw new BusinessRuleException(String.format(
                    "La postulación ya se encuentra en el estado '%s'", actual));
        }

        boolean valida = switch (actual) {
            case POSTULADO -> (nuevo == EstadoPostulacion.EN_REVISION || nuevo == EstadoPostulacion.RECHAZADO);
            case EN_REVISION -> (nuevo == EstadoPostulacion.ENTREVISTA || nuevo == EstadoPostulacion.RECHAZADO);
            case ENTREVISTA -> (nuevo == EstadoPostulacion.CONTRATADO || nuevo == EstadoPostulacion.RECHAZADO);
            default -> false;
        };

        if (!valida) {
            throw new BusinessRuleException(String.format(
                    "Transición no permitida: no se puede pasar del estado '%s' a '%s'", actual, nuevo));
        }
    }
}
