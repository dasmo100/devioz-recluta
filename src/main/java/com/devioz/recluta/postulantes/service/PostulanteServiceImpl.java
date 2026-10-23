package com.devioz.recluta.postulantes.service;

import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.postulaciones.repository.PostulacionRepository;
import com.devioz.recluta.postulantes.dto.PostulanteRequestDTO;
import com.devioz.recluta.postulantes.dto.PostulanteResponseDTO;
import com.devioz.recluta.postulantes.entity.Postulante;
import com.devioz.recluta.postulantes.repository.PostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class PostulanteServiceImpl implements PostulanteService {

    private final PostulanteRepository postulanteRepository;
    private final PostulacionRepository postulacionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PostulanteResponseDTO> buscar(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String termino = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        Page<Postulante> postulantes = postulanteRepository.buscar(termino, pageable);
        return postulantes.map(PostulanteResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PostulanteResponseDTO obtenerPorId(Long id) {
        Postulante postulante = buscarEntidadPorId(id);
        return PostulanteResponseDTO.fromEntity(postulante);
    }

    @Override
    public PostulanteResponseDTO crear(PostulanteRequestDTO request) {
        String dni = request.dni().trim();
        String email = request.email().trim();

        if (postulanteRepository.existsByDni(dni)) {
            throw new BusinessRuleException("Ya existe un postulante registrado con el DNI " + dni);
        }

        if (postulanteRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Ya existe un postulante con el email " + email);
        }

        Postulante postulante = Postulante.builder()
                .nombres(request.nombres().trim())
                .apellidos(request.apellidos().trim())
                .dni(dni)
                .email(email)
                .telefono(request.telefono() != null ? request.telefono().trim() : null)
                .aniosExperiencia(request.aniosExperiencia())
                .habilidades(request.habilidades().trim())
                .fechaRegistro(LocalDate.now())
                .build();

        Postulante guardado = postulanteRepository.save(postulante);
        return PostulanteResponseDTO.fromEntity(guardado);
    }

    @Override
    public PostulanteResponseDTO actualizar(Long id, PostulanteRequestDTO request) {
        Postulante postulante = buscarEntidadPorId(id);

        String dni = request.dni().trim();
        String email = request.email().trim();

        if (postulanteRepository.existsByDniAndIdNot(dni, id)) {
            throw new BusinessRuleException("Ya existe un postulante registrado con el DNI " + dni);
        }

        if (postulanteRepository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessRuleException("Ya existe un postulante con el email " + email);
        }

        postulante.setNombres(request.nombres().trim());
        postulante.setApellidos(request.apellidos().trim());
        postulante.setDni(dni);
        postulante.setEmail(email);
        postulante.setTelefono(request.telefono() != null ? request.telefono().trim() : null);
        postulante.setAniosExperiencia(request.aniosExperiencia());
        postulante.setHabilidades(request.habilidades().trim());

        Postulante actualizado = postulanteRepository.save(postulante);
        return PostulanteResponseDTO.fromEntity(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Postulante postulante = buscarEntidadPorId(id);

        if (postulacionRepository.existsByPostulanteId(id)) {
            throw new BusinessRuleException("No se puede eliminar al postulante porque tiene postulaciones registradas");
        }

        try {
            postulanteRepository.delete(postulante);
            postulanteRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("No se puede eliminar al postulante porque tiene postulaciones registradas");
        }
    }

    private Postulante buscarEntidadPorId(Long id) {
        return postulanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Postulante no encontrado con ID: " + id));
    }
}
