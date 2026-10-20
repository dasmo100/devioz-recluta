package com.devioz.recluta.vacantes.service;

import com.devioz.recluta.exception.BusinessRuleException;
import com.devioz.recluta.exception.ResourceNotFoundException;
import com.devioz.recluta.vacantes.dto.VacanteRequestDTO;
import com.devioz.recluta.vacantes.dto.VacanteResponseDTO;
import com.devioz.recluta.vacantes.model.EstadoVacante;
import com.devioz.recluta.vacantes.model.Vacante;
import com.devioz.recluta.vacantes.repository.VacanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VacanteServiceImpl implements VacanteService {

    private final VacanteRepository vacanteRepository;

    @Override
    public VacanteResponseDTO crear(VacanteRequestDTO request) {
        validarRangoSalarial(request.salarioMinimo(), request.salarioMaximo());

        Vacante vacante = Vacante.builder()
                .titulo(request.titulo().trim())
                .cliente(request.cliente().trim())
                .seniority(request.seniority())
                .salarioMinimo(request.salarioMinimo())
                .salarioMaximo(request.salarioMaximo())
                .modalidad(request.modalidad())
                .estado(EstadoVacante.ABIERTA)
                .build();

        Vacante vacanteGuardada = vacanteRepository.save(vacante);
        return VacanteResponseDTO.fromEntity(vacanteGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacanteResponseDTO> listar(EstadoVacante estado) {
        List<Vacante> vacantes = (estado != null)
                ? vacanteRepository.findByEstado(estado)
                : vacanteRepository.findAll();

        return vacantes.stream()
                .map(VacanteResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VacanteResponseDTO obtenerPorId(Long id) {
        Vacante vacante = buscarEntidadPorId(id);
        return VacanteResponseDTO.fromEntity(vacante);
    }

    @Override
    public VacanteResponseDTO actualizar(Long id, VacanteRequestDTO request) {
        Vacante vacante = buscarEntidadPorId(id);

        if (vacante.getEstado() == EstadoVacante.CERRADA) {
            throw new BusinessRuleException("Una vacante cerrada no se puede editar");
        }

        validarRangoSalarial(request.salarioMinimo(), request.salarioMaximo());

        vacante.setTitulo(request.titulo().trim());
        vacante.setCliente(request.cliente().trim());
        vacante.setSeniority(request.seniority());
        vacante.setSalarioMinimo(request.salarioMinimo());
        vacante.setSalarioMaximo(request.salarioMaximo());
        vacante.setModalidad(request.modalidad());

        Vacante vacanteActualizada = vacanteRepository.save(vacante);
        return VacanteResponseDTO.fromEntity(vacanteActualizada);
    }

    @Override
    public VacanteResponseDTO cerrar(Long id) {
        Vacante vacante = buscarEntidadPorId(id);

        if (vacante.getEstado() == EstadoVacante.CERRADA) {
            throw new BusinessRuleException("La vacante ya se encuentra en estado CERRADA");
        }

        vacante.setEstado(EstadoVacante.CERRADA);
        Vacante vacanteCerrada = vacanteRepository.save(vacante);
        return VacanteResponseDTO.fromEntity(vacanteCerrada);
    }

    private Vacante buscarEntidadPorId(Long id) {
        return vacanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacante", "id", id));
    }

    private void validarRangoSalarial(BigDecimal salarioMinimo, BigDecimal salarioMaximo) {
        if (salarioMinimo != null && salarioMaximo != null && salarioMinimo.compareTo(salarioMaximo) > 0) {
            throw new BusinessRuleException(String.format(
                    "El salario mínimo (%s) debe ser menor o igual al salario máximo (%s)",
                    salarioMinimo, salarioMaximo));
        }
    }
}
