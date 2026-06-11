package smail.sistema_mail_OdontoCool.entities;

import java.math.BigDecimal;
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
@Table(name = "servicio_prestados")
public class ServicioPrestado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    @Column(name = "precio", nullable = false)
    private BigDecimal precio;
    @Column(name = "descuento", nullable = false)
    private BigDecimal descuento;
    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;
    @Column(name = "fecha_servicio", nullable = false)
    private LocalDate fechaServicio;
    @Column(name = "estado", nullable = false)
    private String estado = "ACTIVO";

    @ManyToOne(optional = false)
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(optional = true)
    @JoinColumn(name = "id_boleta", referencedColumnName = "id_boleta")
    private BoletaPago boletaPago;

    public ServicioPrestado() {
    }

    public ServicioPrestado(Integer cantidad, BigDecimal precio, BigDecimal descuento, BigDecimal subtotal,
            LocalDate fechaServicio, Tratamiento tratamiento, Servicio servicio) {
        this.cantidad = cantidad;
        this.precio = precio;
        this.descuento = descuento;
        this.subtotal = subtotal;
        this.fechaServicio = fechaServicio;
        this.tratamiento = tratamiento;
        this.servicio = servicio;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDate getFechaServicio() {
        return fechaServicio;
    }

    public void setFechaServicio(LocalDate fechaServicio) {
        this.fechaServicio = fechaServicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Tratamiento getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(Tratamiento tratamiento) {
        this.tratamiento = tratamiento;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public BoletaPago getBoletaPago() {
        return boletaPago;
    }

    public void setBoletaPago(BoletaPago boletaPago) {
        this.boletaPago = boletaPago;
    }

}
