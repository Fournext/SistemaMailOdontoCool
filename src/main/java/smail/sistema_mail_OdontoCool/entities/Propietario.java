package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "propietarios")
@PrimaryKeyJoinColumn(name = "ci")
public class Propietario extends Persona {

    @Column(name = "fecha_inicio", updatable = false)
    private LocalDate fechaInicio;

    @Column(name = "porcentaje_participacion")
    private BigDecimal porcentajeParticipacion;

    @PrePersist
    public void prePersist() {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now();
        }
    }

    public Propietario() {
    }

    // Getters y Setters

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public BigDecimal getPorcentajeParticipacion() {
        return porcentajeParticipacion;
    }

    public void setPorcentajeParticipacion(BigDecimal porcentajeParticipacion) {
        this.porcentajeParticipacion = porcentajeParticipacion;
    }

}
