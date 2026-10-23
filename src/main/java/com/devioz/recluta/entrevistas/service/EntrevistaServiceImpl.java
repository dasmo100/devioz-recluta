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
import com.devioz.recluta.vacantes.model.EstadoVacante;
import com.devioz.recluta.vacantes.repository.VacanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class EntrevistaServiceImpl implements EntrevistaService {

    private final EntrevistaRepository entrevistaRepository;
    private final PostulacionRepository postulacionRepository;
    private final VacanteRepository vacanteRepository;

    @Override
    public EntrevistaResponseDTO agendar(Long postulacionId, EntrevistaCreateDTO dto) {
        Postulacion postulacion = postulacionRepository.findById(postulacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Postulación no encontrada con ID: " + postulacionId));

        // Regla 1: Solo se agenda si la postulación está exactamente en estado ENTREVISTA
        if (postulacion.getEstado() != EstadoPostulacion.ENTREVISTA) {
            throw new BusinessRuleException(
                    "Solo se pueden agendar entrevistas para postulaciones en estado ENTREVISTA. Estado actual: " + postulacion.getEstado()
            );
        }

        // Regla 2: Nunca se puede agendar en el pasado
        if (dto.fechaHora().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("No se puede agendar una entrevista en una fecha y hora pasada");
        }

        // Regla 3: Cruce de horario para un mismo entrevistador
        LocalDateTime inicioNueva = dto.fechaHora();
        LocalDateTime finNueva = inicioNueva.plusMinutes(dto.duracionMinutos());

        LocalDate fecha = dto.fechaHora().toLocalDate();
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(LocalTime.MAX);

        List<Entrevista> entrevistasActivas = entrevistaRepository
                .findByEntrevistadorIgnoreCaseAndEstadoNotAndFechaHoraBetween(
                        dto.entrevistador().trim(),
                        EstadoEntrevista.CANCELADA,
                        inicioDia,
                        finDia
                );

        for (Entrevista existente : entrevistasActivas) {
            if (existente.getEstado() == EstadoEntrevista.PROGRAMADA || existente.getEstado() == EstadoEntrevista.REALIZADA) {
                LocalDateTime inicioExistente = existente.getFechaHora();
                LocalDateTime finExistente = inicioExistente.plusMinutes(existente.getDuracionMinutos());

                if (inicioNueva.isBefore(finExistente) && finNueva.isAfter(inicioExistente)) {
                    throw new BusinessRuleException("El entrevistador ya tiene una entrevista programada en ese rango de horario");
                }
            }
        }

        Entrevista nuevaEntrevista = Entrevista.builder()
                .postulacion(postulacion)
                .entrevistador(dto.entrevistador().trim())
                .fechaHora(dto.fechaHora())
                .duracionMinutos(dto.duracionMinutos())
                .estado(EstadoEntrevista.PROGRAMADA)
                .puntaje(null)
                .comentario(dto.comentario())
                .build();

        Entrevista guardada = entrevistaRepository.save(nuevaEntrevista);
        return EntrevistaResponseDTO.fromEntity(guardada);
    }

    @Override
    public EntrevistaResponseDTO registrarResultado(Long entrevistaId, EntrevistaResultadoDTO dto) {
        Entrevista entrevista = entrevistaRepository.findById(entrevistaId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrevista no encontrada con ID: " + entrevistaId));

        if (entrevista.getEstado() == EstadoEntrevista.CANCELADA || entrevista.getEstado() == EstadoEntrevista.REALIZADA) {
            throw new BusinessRuleException("No se puede registrar el resultado de una entrevista con estado " + entrevista.getEstado());
        }

        if (dto.estado() == EstadoEntrevista.PROGRAMADA) {
            throw new BusinessRuleException("El nuevo estado de la entrevista debe ser REALIZADA o CANCELADA");
        }

        // Regla 4: Si el nuevo estado es REALIZADA, el puntaje (1 a 5) es estrictamente obligatorio
        if (dto.estado() == EstadoEntrevista.REALIZADA) {
            if (dto.puntaje() == null || dto.puntaje() < 1 || dto.puntaje() > 5) {
                throw new BusinessRuleException("El puntaje (1 a 5) es obligatorio al marcar la entrevista como REALIZADA");
            }
            entrevista.setPuntaje(dto.puntaje());
        } else if (dto.estado() == EstadoEntrevista.CANCELADA) {
            entrevista.setPuntaje(null);
        }

        entrevista.setEstado(dto.estado());
        if (dto.comentario() != null && !dto.comentario().isBlank()) {
            entrevista.setComentario(dto.comentario());
        }

        Entrevista actualizada = entrevistaRepository.save(entrevista);
        return EntrevistaResponseDTO.fromEntity(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntrevistaResponseDTO> listarPorFecha(LocalDate fecha) {
        List<Entrevista> entrevistas;
        if (fecha != null) {
            entrevistas = entrevistaRepository.findByFechaHoraBetweenOrderByFechaHoraAsc(
                    fecha.atStartOfDay(),
                    fecha.atTime(LocalTime.MAX)
            );
        } else {
            entrevistas = entrevistaRepository.findAllByOrderByFechaHoraAsc();
        }

        return entrevistas.stream()
                .map(EntrevistaResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenResponseDTO obtenerResumen() {
        // 1. Total de vacantes abiertas
        long vacantesAbiertas = vacanteRepository.countByEstado(EstadoVacante.ABIERTA);

        // 2. Conteo de postulaciones por cada uno de los 5 estados
        Map<String, Long> postulacionesPorEstado = new LinkedHashMap<>();
        for (EstadoPostulacion estado : EstadoPostulacion.values()) {
            postulacionesPorEstado.put(estado.name(), postulacionRepository.countByEstado(estado));
        }

        // 3. Entrevistas de hoy (PROGRAMADA o REALIZADA)
        LocalDate hoy = LocalDate.now();
        List<EntrevistaResponseDTO> entrevistasDeHoy = entrevistaRepository
                .findByFechaHoraBetweenOrderByFechaHoraAsc(hoy.atStartOfDay(), hoy.atTime(LocalTime.MAX))
                .stream()
                .filter(e -> e.getEstado() != EstadoEntrevista.CANCELADA)
                .map(EntrevistaResponseDTO::fromEntity)
                .toList();

        return new ResumenResponseDTO(vacantesAbiertas, postulacionesPorEstado, entrevistasDeHoy);
    }
}
