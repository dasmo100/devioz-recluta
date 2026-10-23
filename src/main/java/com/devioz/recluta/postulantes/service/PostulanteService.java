package com.devioz.recluta.postulantes.service;

import com.devioz.recluta.postulantes.dto.PostulanteRequestDTO;
import com.devioz.recluta.postulantes.dto.PostulanteResponseDTO;
import org.springframework.data.domain.Page;

public interface PostulanteService {

    Page<PostulanteResponseDTO> buscar(String q, int page, int size);

    PostulanteResponseDTO obtenerPorId(Long id);

    PostulanteResponseDTO crear(PostulanteRequestDTO request);

    PostulanteResponseDTO actualizar(Long id, PostulanteRequestDTO request);

    void eliminar(Long id);
}
