package com.devioz.recluta.vacantes.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vacantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Seniority seniority;

    @Column(name = "salario_minimo", precision = 12, scale = 2)
    private BigDecimal salarioMinimo;

    @Column(name = "salario_maximo", precision = 12, scale = 2)
    private BigDecimal salarioMaximo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Modalidad modalidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoVacante estado = EstadoVacante.ABIERTA;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoVacante.ABIERTA;
        }
    }
}
