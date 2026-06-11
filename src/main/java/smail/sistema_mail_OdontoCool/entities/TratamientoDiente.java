package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tratamientos_dientes")
public class TratamientoDiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "cara_dental", nullable = false)
    private String caraDental;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "estado")
    private String estado = "ACTIVO";

    @Column(name = "tratamiento_planificado", nullable = false, columnDefinition = "TEXT")
    private String tratamientoPlanificado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "diente_id", nullable = false)
    private Diente diente;

    public TratamientoDiente() {
    }

    public TratamientoDiente(String caraDental, String observacion, LocalDate fechaRegistro, String tratamientoPlanificado) {
        this.caraDental = caraDental;
        this.observacion = observacion;
        this.fechaRegistro = fechaRegistro;
        this.tratamientoPlanificado = tratamientoPlanificado;
    }

    public Long getId() {
        return id;
    }

    public String getCaraDental() {
        return caraDental;
    }

    public String getObservacion() {
        return observacion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public String getTratamientoPlanificado() {
        return tratamientoPlanificado;
    }

    public Tratamiento getTratamiento() {
        return tratamiento;
    }

    public Diente getDiente() {
        return diente;
    }

    public void setCaraDental(String caraDental) {
        this.caraDental = caraDental;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setTratamientoPlanificado(String tratamientoPlanificado) {
        this.tratamientoPlanificado = tratamientoPlanificado;
    }

    public void setTratamiento(Tratamiento tratamiento) {
        this.tratamiento = tratamiento;
    }

    public void setDiente(Diente diente) {
        this.diente = diente;
    }

}
