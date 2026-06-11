package smail.sistema_mail_OdontoCool.entities;

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
@Table(name = "sesion_tratamiento")
public class SesionTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "numero_sesion")
    private Integer numeroSesion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "estado")
    private String estado;

    @Column(name = "procedimiento_planificado", columnDefinition = "TEXT")
    private String procedimientoPlanificado;

    @Column(name = "procedimiento_realizado", columnDefinition = "TEXT", nullable = true)
    private String procedimientoRealizado;

    @OneToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;

    public SesionTratamiento() {
    }

    public SesionTratamiento(Integer numeroSesion, String observaciones, String estado, String procedimientoPlanificado,
            Cita cita, Tratamiento tratamiento) {
        this.numeroSesion = numeroSesion;
        this.observaciones = observaciones;
        this.estado = estado;
        this.procedimientoPlanificado = procedimientoPlanificado;
        this.cita = cita;
        this.tratamiento = tratamiento;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumeroSesion() {
        return numeroSesion;
    }

    public void setNumeroSesion(Integer numeroSesion) {
        this.numeroSesion = numeroSesion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getProcedimientoPlanificado() {
        return procedimientoPlanificado;
    }

    public void setProcedimientoPlanificado(String procedimientoPlanificado) {
        this.procedimientoPlanificado = procedimientoPlanificado;
    }

    public String getProcedimientoRealizado() {
        return procedimientoRealizado;
    }

    public void setProcedimientoRealizado(String procedimientoRealizado) {
        this.procedimientoRealizado = procedimientoRealizado;
    }

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }

    public Tratamiento getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(Tratamiento tratamiento) {
        this.tratamiento = tratamiento;
    }

}
