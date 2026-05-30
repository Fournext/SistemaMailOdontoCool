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
@Table(name = "detalle_antecedentes_odontologicos")
public class DetalleAntecedenteOdontologico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_detalle_antecedente;

    @Column(name = "nombre_tratamiento")
    private String nombreTratamiento;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_tratamiento")
    private LocalDate fechaTratamiento;

    @Column(name = "lugar_tratamiento")
    private String lugarTratamiento;

    @Column(name = "observacion")
    private String observacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_antecedente", nullable = false)
    private AntecedenteOdontologico antecedenteOdontologico;

    public DetalleAntecedenteOdontologico() {
    }

    // Getters y Setters
    public Long getIdDetalleAntecendente() {
        return id_detalle_antecedente;
    }

    public String getNombreTratamiento() {
        return nombreTratamiento;
    }

    public void setNombreTratamiento(String nombreTratamiento) {
        this.nombreTratamiento = nombreTratamiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaTratamiento() {
        return fechaTratamiento;
    }

    public void setFechaTratamiento(LocalDate fechaTratamiento) {
        this.fechaTratamiento = fechaTratamiento;
    }

    public String getLugarTratamiento() {
        return lugarTratamiento;
    }

    public void setLugarTratamiento(String lugarTratamiento) {
        this.lugarTratamiento = lugarTratamiento;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public AntecedenteOdontologico getAntecedenteOdontologico() {
        return antecedenteOdontologico;
    }

    public void setAntecedenteOdontologico(AntecedenteOdontologico antecedenteOdontologico) {
        this.antecedenteOdontologico = antecedenteOdontologico;
    }

}