package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

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

    @OneToMany(mappedBy = "doctor")
    private Set<AsignacionTurnoDoctor> asignacionesDoctor = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "doctor_especialidad",
            joinColumns = @JoinColumn(name = "doctor_ci"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id")
    )
    private Set<Especialidad> especialidades = new HashSet<Especialidad>();

    public Doctor() {
    }

    // Getters y Setters
    public Set<Especialidad> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(Set<Especialidad> especialidades) {
        this.especialidades = especialidades;
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

    public Set<AsignacionTurnoDoctor> getAsignacionesDoctor() {
        return asignacionesDoctor;
    }

    public void setAsignacionesDoctor(Set<AsignacionTurnoDoctor> asignacionesDoctor) {
        this.asignacionesDoctor = asignacionesDoctor;
    }

    public void addEspecialidad(Especialidad especialidad) {
        this.especialidades.add(especialidad);
        especialidad.getDoctores().add(this);
    }

    public void addAsignacionDoctor(AsignacionTurnoDoctor asignacion) {
        this.asignacionesDoctor.add(asignacion);
        asignacion.setDoctor(this);
    }

}
