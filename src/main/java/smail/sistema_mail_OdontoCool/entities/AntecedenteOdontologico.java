package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;

@Entity
@Table(name = "antecedentes_odontologicos")
public class AntecedenteOdontologico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_antecedente;

    @Column(name = "observacion_general")
    private String observacionGeneral;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @OneToOne(optional = false)
    @JoinColumn(name = "cod_historial", referencedColumnName = "codigo_historial", nullable = false, unique = true)
    private HistorialClinico historialClinico;

    @OneToMany(mappedBy = "antecedenteOdontologico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleAntecedenteOdontologico> detallesAntecedenteOdontologico;

    @PrePersist
    public void prepErsist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
    }

    public AntecedenteOdontologico() {

    }

    // Getters y Setters
    public Long getIdAntecedente() {
        return id_antecedente;
    }

    public String getObservacionGeneral() {
        return observacionGeneral;
    }

    public void setObservacionGeneral(String observacionGeneral) {
        this.observacionGeneral = observacionGeneral;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public HistorialClinico getHistorialClinico() {
        return historialClinico;
    }

    public void setHistorialClinico(HistorialClinico historialClinico) {
        this.historialClinico = historialClinico;
    }

    public List<DetalleAntecedenteOdontologico> getDetallesAntecedenteOdontologico() {
        return detallesAntecedenteOdontologico;
    }

    public void setDetallesAntecedenteOdontologico(
            List<DetalleAntecedenteOdontologico> detallesAntecedenteOdontologicos) {
        this.detallesAntecedenteOdontologico = detallesAntecedenteOdontologicos;
    }

}
