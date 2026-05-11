package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "doctores")
public class Doctores {

    @Id
    @Column(name = "codigo_doctor")
    private String codigoDoctor;

    @Column(name = "tiempo_experencia")
    private String tiempoExperiencia;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "matricula_profesional", unique = true, nullable = false)
    private String matriculaProfesional;

    @OneToOne
    @JoinColumn(name = "id_persona", referencedColumnName = "ci", unique = true, nullable = false)
    private Personas persona;

    public Doctores() {
    }

    // Getters y Setters
    public String getCodigoDoctor() {
        return codigoDoctor;
    }

    public void setCodigoDoctor(String codigoDoctor) {
        this.codigoDoctor = codigoDoctor;
    }

    public String getTiempoExperiencia() {
        return tiempoExperiencia;
    }

    public void setTiempoExperiencia(String tiempoExperiencia) {
        this.tiempoExperiencia = tiempoExperiencia;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public String getMatriculaProfesional() {
        return matriculaProfesional;
    }

    public void setMatriculaProfesional(String matriculaProfesional) {
        this.matriculaProfesional = matriculaProfesional;
    }

    public Personas getPersona() {
        return persona;
    }

    public void setPersona(Personas persona) {
        this.persona = persona;
    }
}
