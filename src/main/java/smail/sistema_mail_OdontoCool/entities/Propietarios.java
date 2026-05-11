package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "propietarios")
public class Propietarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "porcentaje_participacion")
    private BigDecimal porcentajeParticipacion;

    @OneToOne
    @JoinColumn(name = "id_persona", referencedColumnName = "ci", unique = true, nullable = false)
    private Personas persona;

    public Propietarios() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Personas getPersona() {
        return persona;
    }

    public void setPersona(Personas persona) {
        this.persona = persona;
    }
}
