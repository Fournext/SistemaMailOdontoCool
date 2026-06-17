package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detalle_recomendacion")
public class DetalleRecomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "dosis")
    private String dosis;

    @Column(name = "duracion")
    private String duracion;

    @Column(name = "frecuencia")
    private String frecuencia;

    @ManyToOne(optional = false)
     @JoinColumn(name = "receta_recomendacion_id", nullable = false)
    private RecetaRecomendacion recetaRecomendacion;

    public DetalleRecomendacion() {
    }

    public DetalleRecomendacion(String descripcion, String dosis, String duracion, String frecuencia, RecetaRecomendacion recetaRecomendacion) {
        this.descripcion = descripcion;
        this.dosis = dosis;
        this.duracion = duracion;
        this.frecuencia = frecuencia;
        this.recetaRecomendacion = recetaRecomendacion;
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public RecetaRecomendacion getRecetaRecomendacion() {
        return recetaRecomendacion;
    }

    public void setRecetaRecomendacion(RecetaRecomendacion recetaRecomendacion) {
        this.recetaRecomendacion = recetaRecomendacion;
    }

}
