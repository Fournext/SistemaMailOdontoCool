package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "secretarias")
public class Secretaria {

    @Id
    @Column(name = "ci", nullable = false, length = 20)
    private String ci;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "ci")
    private Persona persona;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @OneToMany(mappedBy = "secretaria")
    private List<Cita> citas = new ArrayList<>();

    @OneToMany(mappedBy = "secretaria")
    private Set<AsignacionTurnoSecretaria> asignacionesSecretaria = new HashSet<>();

    @OneToMany(mappedBy = "secretaria")
    private List<BoletaPago> boletaPagos = new ArrayList<>();

    public Secretaria() {
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

    // --- Secretaria Specific Getters & Setters ---
    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
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
