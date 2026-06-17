package smail.sistema_mail_OdontoCool.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "diagnosticos")
public class Diagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sintomas", nullable = false, columnDefinition = "TEXT")
    private String sintomas;

    @Column(name = "tipo_diagnostico", nullable = false)
    private String tipoDiagnostico;

    @Column(name = "gravedad", nullable = false)
    private String gravedad;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToOne(optional = false)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @OneToMany(mappedBy = "diagnostico", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DetalleDiagnostico> detallesDiagnostico = new HashSet<>();

    public Diagnostico() {
    }

    public Diagnostico(String sintomas, String tipoDiagnostico, String gravedad, String observaciones) {
        this.sintomas = sintomas;
        this.tipoDiagnostico = tipoDiagnostico;
        this.gravedad = gravedad;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public String getSintomas() {
        return sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }

    public String getTipoDiagnostico() {
        return tipoDiagnostico;
    }

    public void setTipoDiagnostico(String tipoDiagnostico) {
        this.tipoDiagnostico = tipoDiagnostico;
    }

    public String getGravedad() {
        return gravedad;
    }

    public void setGravedad(String gravedad) {
        this.gravedad = gravedad;
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

    public Set<DetalleDiagnostico> getDetallesDiagnostico() {
        return detallesDiagnostico;
    }

    public void setDetallesDiagnostico(Set<DetalleDiagnostico> detallesDiagnostico) {
        this.detallesDiagnostico = detallesDiagnostico;
    }

    public void addDetalleDiagnostico(DetalleDiagnostico detalle) {
        this.detallesDiagnostico.add(detalle);
        detalle.setDiagnostico(this);
    }

}
