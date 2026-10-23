package com.devioz.recluta.postulaciones.repository;

import com.devioz.recluta.postulaciones.entity.EstadoPostulacion;
import com.devioz.recluta.postulaciones.entity.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    boolean existsByPostulanteIdAndVacanteId(Long postulanteId, Long vacanteId);

    boolean existsByPostulanteId(Long postulanteId);

    List<Postulacion> findByVacanteId(Long vacanteId);

    long countByEstado(EstadoPostulacion estado);

    interface ConteoPorEstado {
        EstadoPostulacion getEstado();
        Long getTotal();
    }

    @Query("SELECT p.estado AS estado, COUNT(p) AS total FROM Postulacion p GROUP BY p.estado")
    List<ConteoPorEstado> contarPostulacionesPorEstado();
}
