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
@Table(name = "servicios")
public class Servicio {
    // La clase Servicio es una entidad que representa los servicios odontológicos que ofrece la clínica. 
    // Cada servicio tiene un nombre, una descripción y un estado (activo o inactivo). 
    // Además, cada servicio puede tener múltiples precios asociados a lo largo del tiempo, lo que permite llevar un historial de precios para cada servicio.   

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "estado")
    private String estado = "ACTIVO";

    @OneToMany(mappedBy = "servicio")
    private Set<AsignacionPrecio> asignacionesPrecio = new HashSet<>();

    public Servicio() {
    }

    public Servicio(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEstado() {
        return estado;
    }

    public Set<AsignacionPrecio> getAsignacionesPrecio() {
        return asignacionesPrecio;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void addAsignacionPrecio(AsignacionPrecio asignacion) {
        this.asignacionesPrecio.add(asignacion);
        asignacion.setServicio(this);
    }

}
