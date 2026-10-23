package com.devioz.recluta.entrevistas.service;

import com.devioz.recluta.entrevistas.dto.EntrevistaCreateDTO;
import com.devioz.recluta.entrevistas.dto.EntrevistaResponseDTO;
import com.devioz.recluta.entrevistas.dto.EntrevistaResultadoDTO;
import com.devioz.recluta.entrevistas.dto.ResumenResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface EntrevistaService {

    EntrevistaResponseDTO agendar(Long postulacionId, EntrevistaCreateDTO dto);

    EntrevistaResponseDTO registrarResultado(Long entrevistaId, EntrevistaResultadoDTO dto);

    List<EntrevistaResponseDTO> listarPorFecha(LocalDate fecha);

    ResumenResponseDTO obtenerResumen();
}
