package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "historiales_clinicos")
public class HistorialClinico {
    @Id
    @Column(name = "codigo_historial", nullable = false)
    private String codigoHistorial;

    @Column(name = "alergias")
    private String alergias;

    @Column(name = "antecedentes_medicos")
    private String antencedentesMedicos;

    @Column(name = "enfermedades_base")
    private String enfermedadesBase;

    @Column(name = "motivo_apertura")
    private String motivoApertura;

    @Column(name = "fecha_apertura")
    private LocalDate fechaApertura;

    @Column(name = "fecha_actualizacion")
    private LocalDate fechaActualizacion;

    @Column(name = "observaciones_generales")
    private String observacionesGenerales;

    @Column(name = "estado")
    private String estado;

    @OneToMany(mappedBy = "historialClinico")
    private List<Cita> citas = new ArrayList<>();

    @OneToOne(optional = false)
    @JoinColumn(name = "ci_paciente", referencedColumnName = "ci", nullable = false, unique = true)
    private Paciente paciente;

    public HistorialClinico() {
    }

    @PrePersist
    public void prePersist() {
        if (fechaApertura == null) {
            fechaApertura = LocalDate.now();
        }

        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDate.now();
        }

        if (estado == null) {
            estado = "ACTIVO";
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDate.now();
    }

    // Getter y Setters
    public String getCodigoHistorial() {
        return codigoHistorial;
    }

    public void setCodigoHistorial(String codigoHistorial) {
        this.codigoHistorial = codigoHistorial;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getAntencedentesMedicos() {
        return antencedentesMedicos;
    }

    public void setAntencedentesMedicos(String antencedentesMedicos) {
        this.antencedentesMedicos = antencedentesMedicos;
    }

    public String getEnfermedadesBase() {
        return enfermedadesBase;
    }

    public void setEnfermedadesBase(String enfermedadesBase) {
        this.enfermedadesBase = enfermedadesBase;
    }

    public String getMotivoApertura() {
        return motivoApertura;
    }

    public void setMotivoApertura(String motivoApertura) {
        this.motivoApertura = motivoApertura;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public LocalDate getFechaActualizacion() {
        return fechaActualizacion;
    }

    public String getObservacionesGenerales() {
        return observacionesGenerales;
    }

    public void setObservacionesGenerales(String observacionesGenerales) {
        this.observacionesGenerales = observacionesGenerales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

}
