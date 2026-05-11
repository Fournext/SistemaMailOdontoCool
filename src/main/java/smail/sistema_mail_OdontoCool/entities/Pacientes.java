package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pacientes")
public class Pacientes {

    @Id
    @Column(name = "codigo_paciente")
    private String codigoPaciente;

    @Column(name = "nombre_contacto_emergencia")
    private String nombreContactoEmergencia;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private LocalDate fechaRegistro;

    @Column(name = "telefono_emergencia")
    private String telefonoEmergencia;

    @OneToOne
    @JoinColumn(name = "id_persona", referencedColumnName = "ci", unique = true, nullable = false)
    private Personas persona;

    public Pacientes() {
    }

    // Getters y Setters
    public String getCodigoPaciente() {
        return codigoPaciente;
    }

    public void setCodigoPaciente(String codigoPaciente) {
        this.codigoPaciente = codigoPaciente;
    }

    public String getNombreContactoEmergencia() {
        return nombreContactoEmergencia;
    }

    public void setNombreContactoEmergencia(String nombreContactoEmergencia) {
        this.nombreContactoEmergencia = nombreContactoEmergencia;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getTelefonoEmergencia() {
        return telefonoEmergencia;
    }

    public void setTelefonoEmergencia(String telefonoEmergencia) {
        this.telefonoEmergencia = telefonoEmergencia;
    }

    public Personas getPersona() {
        return persona;
    }

    public void setPersona(Personas persona) {
        this.persona = persona;
    }
}
