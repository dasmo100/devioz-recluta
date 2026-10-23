package com.devioz.recluta.postulaciones.repository;

import com.devioz.recluta.postulaciones.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findByPostulacionIdOrderByFechaAsc(Long postulacionId);
}
