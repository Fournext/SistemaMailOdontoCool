package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
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
@Table(name = "receta_recomendacion")
public class RecetaRecomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;

    @OneToMany(mappedBy = "recetaRecomendacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DetalleRecomendacion> detallesRecomendacion = new HashSet<>();

    public RecetaRecomendacion() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Tratamiento getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(Tratamiento tratamiento) {
        this.tratamiento = tratamiento;
    }

    public RecetaRecomendacion(LocalDate fechaEmision, String observaciones, Tratamiento tratamiento) {
        this.fechaEmision = fechaEmision;
        this.observaciones = observaciones;
        this.tratamiento = tratamiento;
    }

    public Set<DetalleRecomendacion> getDetallesRecomendacion() {
        return detallesRecomendacion;
    }

    public void setDetallesRecomendacion(Set<DetalleRecomendacion> detallesRecomendacion) {
        this.detallesRecomendacion = detallesRecomendacion;
    }

    public void addDetalleRecomendacion(DetalleRecomendacion detalle) {
        detallesRecomendacion.add(detalle);
        detalle.setRecetaRecomendacion(this);
    }

    public void removeDetalleRecomendacion(DetalleRecomendacion detalle) {
        detallesRecomendacion.remove(detalle);
        detalle.setRecetaRecomendacion(null);
    }

    


    

}
