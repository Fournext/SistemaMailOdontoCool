package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detalle_boleta")
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_detalle_boleta;

    @Column(name = "precio_unitario")
    private double precioUnitario;

    @Column(name = "cantidad")
    private int cantidad;

    @Column(name = "subtotal")
    private double subtotal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_boleta", nullable = false)
    private BoletaPago boletaPago;

    public DetalleBoleta() {
    }

    // Getter y Setters

    public Long getId_detalle_boleta() {
        return id_detalle_boleta;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public BoletaPago getBoletaPago() {
        return boletaPago;
    }

    public void setBoletaPago(BoletaPago boletaPago) {
        this.boletaPago = boletaPago;
    }

}
