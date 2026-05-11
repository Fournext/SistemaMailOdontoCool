package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "secretarias")
public class Secretarias {

    @Id
    @Column(name = "codigo_secretaria")
    private String codigoSecretaria;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @OneToOne
    @JoinColumn(name = "id_persona", referencedColumnName = "ci", unique = true, nullable = false)
    private Personas persona;

    public Secretarias() {
    }

    // Getters y Setters
    public String getCodigoSecretaria() {
        return codigoSecretaria;
    }

    public void setCodigoSecretaria(String codigoSecretaria) {
        this.codigoSecretaria = codigoSecretaria;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public Personas getPersona() {
        return persona;
    }

    public void setPersona(Personas persona) {
        this.persona = persona;
    }
}
