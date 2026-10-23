package com.devioz.recluta.postulaciones.service;

import com.devioz.recluta.postulaciones.dto.CambioEstadoDTO;
import com.devioz.recluta.postulaciones.dto.HistorialEstadoResponseDTO;
import com.devioz.recluta.postulaciones.dto.PostulacionCreateDTO;
import com.devioz.recluta.postulaciones.dto.PostulacionResponseDTO;

import java.util.List;

public interface PostulacionService {

    PostulacionResponseDTO crear(Long vacanteId, PostulacionCreateDTO dto);

    List<PostulacionResponseDTO> listarPorVacante(Long vacanteId);

    PostulacionResponseDTO cambiarEstado(Long postulacionId, CambioEstadoDTO dto);

    List<HistorialEstadoResponseDTO> consultarHistorial(Long postulacionId);
}
