package com.devioz.recluta.vacantes.repository;

import com.devioz.recluta.vacantes.model.EstadoVacante;
import com.devioz.recluta.vacantes.model.Vacante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacanteRepository extends JpaRepository<Vacante, Long> {

    List<Vacante> findByEstado(EstadoVacante estado);
}
