package com.devioz.recluta.postulantes.repository;

import com.devioz.recluta.postulantes.entity.Postulante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostulanteRepository extends JpaRepository<Postulante, Long> {

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT p FROM Postulante p WHERE :q IS NULL OR TRIM(:q) = '' OR " +
           "LOWER(p.nombres) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.dni) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Postulante> buscar(@Param("q") String q, Pageable pageable);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM postulaciones WHERE postulante_id = :postulanteId", nativeQuery = true)
    boolean tienePostulaciones(@Param("postulanteId") Long postulanteId);
}
