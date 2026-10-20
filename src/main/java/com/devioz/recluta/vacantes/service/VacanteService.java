package com.devioz.recluta.vacantes.service;

import com.devioz.recluta.vacantes.dto.VacanteRequestDTO;
import com.devioz.recluta.vacantes.dto.VacanteResponseDTO;
import com.devioz.recluta.vacantes.model.EstadoVacante;

import java.util.List;

public interface VacanteService {

    VacanteResponseDTO crear(VacanteRequestDTO request);

    List<VacanteResponseDTO> listar(EstadoVacante estado);

    VacanteResponseDTO obtenerPorId(Long id);

    VacanteResponseDTO actualizar(Long id, VacanteRequestDTO request);

    VacanteResponseDTO cerrar(Long id);
}
