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
@Table(name = "analisis")
public class Analisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado")
    private String estado = "ACTIVO";

    @OneToMany(mappedBy = "analisis")
    private Set<SolicitudAnalisis> solicitudesAnalisis = new HashSet<>();

    public Analisis() {
    }

    public Analisis(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Set<SolicitudAnalisis> getSolicitudesAnalisis() {
        return solicitudesAnalisis;
    }

    public void setSolicitudesAnalisis(Set<SolicitudAnalisis> solicitudesAnalisis) {
        this.solicitudesAnalisis = solicitudesAnalisis;
    }

    public void agregarSolicitudAnalisis(SolicitudAnalisis solicitud) {
        solicitudesAnalisis.add(solicitud);
        solicitud.setAnalisis(this);
    }

}
