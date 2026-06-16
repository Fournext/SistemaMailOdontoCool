package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tratamientos")
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "objetivo_tratamiento", nullable = false, columnDefinition = "TEXT")
    private String objetivoTratamiento;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFin;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historial_clinico_id", nullable = false)
    private HistorialClinico historialClinico;

    @OneToMany(mappedBy = "tratamiento")
    private Set<TratamientoDiente> tratamientosDiente = new HashSet<TratamientoDiente>();

    @OneToMany(mappedBy = "tratamiento")
    private Set<RecetaRecomendacion> recetasRecomendaciones = new HashSet<>();

    @OneToMany(mappedBy = "tratamiento")
    private Set<SolicitudAnalisis> solicitudesAnalisis = new HashSet<>();

    public Tratamiento() {
    }

    public Tratamiento(String objetivoTratamiento, String observacion, String estado, LocalDate fechaInicio,
            LocalDate fechaFin, LocalDate fechaFinReal) {
        this.objetivoTratamiento = objetivoTratamiento;
        this.observacion = observacion;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaFinReal = fechaFinReal;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public String getObjetivoTratamiento() {
        return objetivoTratamiento;
    }

    public String getObservacion() {
        return observacion;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public LocalDate getFechaFinReal() {
        return fechaFinReal;
    }

    public Set<SolicitudAnalisis> getSolicitudesAnalisis() {
        return solicitudesAnalisis;
    }

    public void setSolicitudesAnalisis(Set<SolicitudAnalisis> solicitudesAnalisis) {
        this.solicitudesAnalisis = solicitudesAnalisis;
    }

    public void setObjetivoTratamiento(String objetivoTratamiento) {
        this.objetivoTratamiento = objetivoTratamiento;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setFechaFinReal(LocalDate fechaFinReal) {
        this.fechaFinReal = fechaFinReal;
    }

    public HistorialClinico getHistorialClinico() {
        return historialClinico;
    }

    public void setHistorialClinico(HistorialClinico historialClinico) {
        this.historialClinico = historialClinico;
    }

    public Set<TratamientoDiente> getTratamientosDiente() {
        return tratamientosDiente;
    }

    public void addTratamientoDiente(TratamientoDiente tratamientoDiente) {
        this.tratamientosDiente.add(tratamientoDiente);
        tratamientoDiente.setTratamiento(this);
    }

    public Set<RecetaRecomendacion> getRecetasRecomendaciones() {
        return recetasRecomendaciones;
    }

    public void setRecetasRecomendaciones(Set<RecetaRecomendacion> recetasRecomendaciones) {
        this.recetasRecomendaciones = recetasRecomendaciones;
    }

}
