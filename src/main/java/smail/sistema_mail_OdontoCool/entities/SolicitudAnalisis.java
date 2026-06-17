package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitud_analisis")
public class SolicitudAnalisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "estado")
    private String estado = "ACTIVO";

    @ManyToOne(optional = false)
    @JoinColumn(name = "analisis_id", nullable = false)
    private Analisis analisis;

    @ManyToOne(optional = true)
    @JoinColumn(name = "tratamiento_id", nullable = true)
    private Tratamiento tratamiento;

    @OneToOne(mappedBy = "solicitudAnalisis")
    private ResultadoAnalisis resultadoAnalisis;

    public SolicitudAnalisis() {
    }

    public SolicitudAnalisis(LocalDate fechaSolicitud, String motivo , String estado, Analisis analisis, Tratamiento tratamiento) {
        this.fechaSolicitud = fechaSolicitud;
        this.motivo = motivo;
        this.estado = estado;
        this.analisis = analisis;
        this.tratamiento = tratamiento;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Analisis getAnalisis() {
        return analisis;
    }

    public void setAnalisis(Analisis analisis) {
        this.analisis = analisis;
    }

    public ResultadoAnalisis getResultadoAnalisis() {
        return resultadoAnalisis;
    }

    public void setResultadoAnalisis(ResultadoAnalisis resultadoAnalisis) {
        this.resultadoAnalisis = resultadoAnalisis;
    }

    public Tratamiento getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(Tratamiento tratamiento) {
        this.tratamiento = tratamiento;
    }

}
