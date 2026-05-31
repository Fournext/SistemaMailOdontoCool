package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "secretarias")
@PrimaryKeyJoinColumn(name = "ci")
public class Secretaria extends Persona {

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @OneToMany(mappedBy = "secretaria")
    private Set<AsignacionTurnoSecretaria> asignacionesSecretaria = new HashSet<>();

    public Secretaria() {
    }

    // Getters y Setters
    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public Set<AsignacionTurnoSecretaria> getAsignacionesSecretaria() {
        return asignacionesSecretaria;
    }

    public void setAsignacionesSecretaria(Set<AsignacionTurnoSecretaria> asignacionesSecretaria) {
        this.asignacionesSecretaria = asignacionesSecretaria;
    }

    public void addAsignacionSecretaria(AsignacionTurnoSecretaria asignacion) {
        this.asignacionesSecretaria.add(asignacion);
        asignacion.setSecretaria(this);
    }

}
