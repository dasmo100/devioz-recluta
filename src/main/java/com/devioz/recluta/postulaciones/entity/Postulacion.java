package com.devioz.recluta.postulaciones.entity;

import com.devioz.recluta.postulantes.entity.Postulante;
import com.devioz.recluta.vacantes.model.Vacante;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "postulaciones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_postulaciones_postulante_vacante",
                        columnNames = {"postulante_id", "vacante_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postulante_id", nullable = false)
    private Postulante postulante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacante_id", nullable = false)
    private Vacante vacante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoPostulacion estado = EstadoPostulacion.POSTULADO;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoPostulacion.POSTULADO;
        }
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
