package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @Column(name = "ci", nullable = false, length = 20)
    private String ci;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @MapsId
    @JoinColumn(name = "ci")
    private Persona persona;

    @Column(name = "nombre_contacto_emergencia")
    private String nombreContactoEmergencia;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDate fechaRegistro;

    @Column(name = "telefono_emergencia")
    private String telefonoEmergencia;

    @OneToMany(mappedBy = "paciente")
    private List<Cita> citas = new ArrayList<>();

    @OneToMany(mappedBy = "paciente")
    private List<BoletaPago> boletasPago = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
    }

    public Paciente() {
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

    // --- Paciente Specific Getters & Setters ---
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

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }
}
