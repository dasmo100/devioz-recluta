package com.devioz.recluta.entrevistas.entity;

import com.devioz.recluta.postulaciones.entity.Postulacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entrevistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrevista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postulacion_id", nullable = false)
    private Postulacion postulacion;

    @Column(nullable = false)
    private String entrevistador;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoEntrevista estado = EstadoEntrevista.PROGRAMADA;

    @Column(name = "puntaje")
    private Integer puntaje;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoEntrevista.PROGRAMADA;
        }
    }
}
