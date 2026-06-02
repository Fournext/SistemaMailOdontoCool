package smail.sistema_mail_OdontoCool.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "hora_inicio", nullable = false)
    private String horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private String horaFin;

    @Column(name = "estado", nullable = false)
    private String estado;

    @OneToMany(mappedBy = "turno")
    private Set<AsignacionTurnoSecretaria> asignacionesSecretaria = new HashSet<>();

    @OneToMany(mappedBy = "turno")
    private Set<AsignacionTurnoDoctor> asignacionesDoctor = new HashSet<>();

    public Turno() {
    }

    public Turno(String nombre, String horaInicio, String horaFin, String estado) {
        this.nombre = nombre;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String hora_inicio) {
        this.horaInicio = hora_inicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String hora_fin) {
        this.horaFin = hora_fin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Set<AsignacionTurnoSecretaria> getAsignacionesSecretaria() {
        return asignacionesSecretaria;
    }

    public Set<AsignacionTurnoDoctor> getAsignacionesDoctor() {
        return asignacionesDoctor;
    }

    public void addAsignacionSecretaria(AsignacionTurnoSecretaria asignacion) {
        this.asignacionesSecretaria.add(asignacion);
        asignacion.setTurno(this);
    }

    public void addAsignacionDoctor(AsignacionTurnoDoctor asignacion) {
        this.asignacionesDoctor.add(asignacion);
        asignacion.setTurno(this);
    }

}
