package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignacion_estado_cita")
public class AsignacionEstadoCita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion_estado")
    private Long idAsignacionEstado;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "observaciones")
    private String observaciones;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cita", referencedColumnName = "id_cita", nullable = false)
    private Cita cita;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_estado_cita", referencedColumnName = "id_estado_cita", nullable = false)
    private EstadoCita estadoCita;

    @PrePersist
    public void prePersist() {
        if (fechaCambio == null) {
            fechaCambio = LocalDateTime.now();
        }
    }

    public AsignacionEstadoCita() {
    }

    // Getters y Setters
    public Long getIdAsignacionEstado() {
        return idAsignacionEstado;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }

    public EstadoCita getEstadoCita() {
        return estadoCita;
    }

    public void setEstadoCita(EstadoCita estadoCita) {
        this.estadoCita = estadoCita;
    }

}
