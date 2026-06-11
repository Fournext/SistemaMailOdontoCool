package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resultados_analisis")
public class ResultadoAnalisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fecha_resultado")
    private LocalDate fechaResultado;

    @Column(name = "resultado")
    private String resultado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "interpretacion", columnDefinition = "TEXT")
    private String interpretacion;

    @Column(name = "archivo_adjunto", nullable = true)
    private String archivoAdjunto;

    @Column(name = "estado")
    private String estado = "ACTIVO";

    @OneToOne(optional = false)
    @JoinColumn(name = "solicitud_analisis_id", nullable = false)
    private SolicitudAnalisis solicitudAnalisis;

    public ResultadoAnalisis() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFechaResultado() {
        return fechaResultado;
    }

    public void setFechaResultado(LocalDate fechaResultado) {
        this.fechaResultado = fechaResultado;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getArchivoAdjunto() {
        return archivoAdjunto;
    }

    public void setArchivoAdjunto(String archivoAdjunto) {
        this.archivoAdjunto = archivoAdjunto;
    }

    public String getInterpretacion() {
        return interpretacion;
    }

    public void setInterpretacion(String interpretacion) {
        this.interpretacion = interpretacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public SolicitudAnalisis getSolicitudAnalisis() {
        return solicitudAnalisis;
    }

    public void setSolicitudAnalisis(SolicitudAnalisis solicitudAnalisis) {
        this.solicitudAnalisis = solicitudAnalisis;
    }

}
