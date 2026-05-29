package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "doctores")
public class Doctor {

    @Id
    @Column(name = "ci", nullable = false, length = 20)
    private String ci;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "ci")
    private Persona persona;

    @Column(name = "tiempo_experencia")
    private String tiempoExperiencia;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "matricula_profesional", unique = true, nullable = false)
    private String matriculaProfesional;

    @Column(name = "telefono_profesional")
    private String telefonoProfesional;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "doctor_especialidad",
            joinColumns = @JoinColumn(name = "doctor_ci"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id")
    )
    private Set<Especialidad> especialidades = new HashSet<>();

    public Doctor() {
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    // --- Delegation Methods to Persona ---
    public String getCi() {
        return persona != null ? persona.getCi() : ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
        if (persona != null) {
            persona.setCi(ci);
        }
    }

    public String getNombres() {
        return persona != null ? persona.getNombres() : null;
    }

    public void setNombres(String nombres) {
        if (persona != null) {
            persona.setNombres(nombres);
        }
    }

    public String getApellidos() {
        return persona != null ? persona.getApellidos() : null;
    }

    public void setApellidos(String apellidos) {
        if (persona != null) {
            persona.setApellidos(apellidos);
        }
    }

    public String getDireccion() {
        return persona != null ? persona.getDireccion() : null;
    }

    public void setDireccion(String direccion) {
        if (persona != null) {
            persona.setDireccion(direccion);
        }
    }

    public String getGenero() {
        return persona != null ? persona.getGenero() : null;
    }

    public void setGenero(String genero) {
        if (persona != null) {
            persona.setGenero(genero);
        }
    }

    public String getTelefono() {
        return persona != null ? persona.getTelefono() : null;
    }

    public void setTelefono(String telefono) {
        if (persona != null) {
            persona.setTelefono(telefono);
        }
    }

    public String getEstado() {
        return persona != null ? persona.getEstado() : null;
    }

    public void setEstado(String estado) {
        if (persona != null) {
            persona.setEstado(estado);
        }
    }

    public LocalDate getFechaNacimiento() {
        return persona != null ? persona.getFechaNacimiento() : null;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (persona != null) {
            persona.setFechaNacimiento(fechaNacimiento);
        }
    }

    public LocalDate getFechaRegistro() {
        return persona != null ? persona.getFechaRegistro() : null;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        if (persona != null) {
            persona.setFechaRegistro(fechaRegistro);
        }
    }

    // --- Doctor Specific Getters & Setters ---
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

    public String getTelefonoProfesional() {
        return telefonoProfesional;
    }

    public void setTelefonoProfesional(String telefonoProfesional) {
        this.telefonoProfesional = telefonoProfesional;
    }
}
