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
@Table(name = "detalle_diagnostico")
public class DetalleDiagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "zona_bucal", nullable = false)
    private String zonaBucal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "diagnostico_id", nullable = false)
    private Diagnostico diagnostico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "diente_id", nullable = false)
    private Diente diente;

    public DetalleDiagnostico() {
    }

    public Diagnostico getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(Diagnostico diagnostico) {
        this.diagnostico = diagnostico;
    }

    public Long getId() {
        return id;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getZonaBucal() {
        return zonaBucal;
    }

    public void setZonaBucal(String zonaBucal) {
        this.zonaBucal = zonaBucal;
    }

    public Diente getDiente() {
        return diente;
    }

    public void setDiente(Diente diente) {
        this.diente = diente;
    }

    public void addDiente(Diente diente) {
        this.diente = diente;
        diente.getDetallesDiagnostico().add(this);
    }
}
