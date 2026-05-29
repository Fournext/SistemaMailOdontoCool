package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "propietarios")
public class Propietario {

    @Id
    @Column(name = "ci", nullable = false, length = 20)
    private String ci;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "ci")
    private Persona persona;

    @Column(name = "fecha_inicio", updatable = false)
    private LocalDate fechaInicio;

    @Column(name = "porcentaje_participacion")
    private BigDecimal porcentajeParticipacion;

    @PrePersist
    public void prePersist() {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now();
        }
    }

    public Propietario() {
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

    // --- Propietario Specific Getters & Setters ---
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public BigDecimal getPorcentajeParticipacion() {
        return porcentajeParticipacion;
    }

    public void setPorcentajeParticipacion(BigDecimal porcentajeParticipacion) {
        this.porcentajeParticipacion = porcentajeParticipacion;
    }
}
