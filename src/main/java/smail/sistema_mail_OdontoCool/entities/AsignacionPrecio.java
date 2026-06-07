package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignacion_precios")
public class AsignacionPrecio {

    // La clase AsignacionPrecio es una entidad que representa la relación entre un servicio y un precio específico. 
    // Esta clase permite asignar un precio a un servicio en un período de tiempo determinado, lo que facilita la gestión de cambios de precios a lo largo del tiempo. 
    // Cada asignación de precio tiene una fecha de inicio y una fecha de fin, lo que permite llevar un historial de precios para cada servicio.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = true)
    private LocalDate fechaFin;

    @Column(name = "estado")
    private String estado = "ACTIVO";

    @ManyToOne()
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne()
    @JoinColumn(name = "precio_id", nullable = false)
    private Precio precio;

    public AsignacionPrecio() {
    }

    public AsignacionPrecio(LocalDate fechaInicio, LocalDate fechaFin, Servicio servicio, Precio precio) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.servicio = servicio;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public Precio getPrecio() {
        return precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public void setPrecio(Precio precio) {
        this.precio = precio;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
