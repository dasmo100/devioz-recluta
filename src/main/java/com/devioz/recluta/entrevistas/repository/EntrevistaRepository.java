package com.devioz.recluta.entrevistas.repository;

import com.devioz.recluta.entrevistas.entity.EstadoEntrevista;
import com.devioz.recluta.entrevistas.entity.Entrevista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntrevistaRepository extends JpaRepository<Entrevista, Long> {

    List<Entrevista> findByFechaHoraBetweenOrderByFechaHoraAsc(LocalDateTime start, LocalDateTime end);

    List<Entrevista> findAllByOrderByFechaHoraAsc();

    List<Entrevista> findByEntrevistadorIgnoreCaseAndEstadoNotAndFechaHoraBetween(
            String entrevistador,
            EstadoEntrevista estado,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Entrevista> findByEntrevistadorIgnoreCaseAndEstadoNot(
            String entrevistador,
            EstadoEntrevista estado
    );
}
