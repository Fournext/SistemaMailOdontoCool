package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "secretarias")
@PrimaryKeyJoinColumn(name = "ci")
public class Secretaria extends Persona {

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    public Secretaria() {
    }

    // Getters y Setters

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

}
