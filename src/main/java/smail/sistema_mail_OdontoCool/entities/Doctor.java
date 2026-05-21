package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "doctores")
@PrimaryKeyJoinColumn(name = "ci")
public class Doctor extends Persona {

    @Column(name = "tiempo_experencia")
    private String tiempoExperiencia;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "matricula_profesional", unique = true, nullable = false)
    private String matriculaProfesional;

    public Doctor() {
    }

    // Getters y Setters

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

}
