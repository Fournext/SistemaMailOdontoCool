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
@Table(name = "dientes")
public class Diente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "ubicacion", nullable = false)
    private String ubicacion;

    @Column(name = "estado", nullable = true)
    private String estado = "ACTIVO";

    @OneToMany(mappedBy = "diente")
    private Set<TratamientoDiente> tratamientosDiente = new HashSet<TratamientoDiente>();

    @OneToMany(mappedBy = "diente")
    private Set<DetalleDiagnostico> detallesDiagnostico = new HashSet<DetalleDiagnostico>();

    public Diente() {
    }

    public Diente(String nombre, Integer numero, String tipo, String ubicacion) {
        this.nombre = nombre;
        this.numero = numero;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getNumero() {
        return numero;
    }

    public String getTipo() {
        return tipo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public Set<TratamientoDiente> getTratamientosDiente() {
        return tratamientosDiente;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void addTratamientoDiente(TratamientoDiente tratamiento) {
        this.tratamientosDiente.add(tratamiento);
        tratamiento.setDiente(this);
    }

    public void addDetalleDiagnostico(DetalleDiagnostico detalle) {
        this.detallesDiagnostico.add(detalle);
        detalle.setDiente(this);
    }

    public Set<DetalleDiagnostico> getDetallesDiagnostico() {
        return detallesDiagnostico;
    }

}
