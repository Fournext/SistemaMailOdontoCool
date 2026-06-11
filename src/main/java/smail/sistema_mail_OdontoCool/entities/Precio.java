package smail.sistema_mail_OdontoCool.entities;

import java.math.BigDecimal;
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
@Table(name = "precios")
public class Precio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "moneda", nullable = false)
    private String moneda;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "estado")
    private String estado = "ACTIVO";

    @OneToMany(mappedBy = "precio")
    private Set<AsignacionPrecio> asignacionesPrecio = new HashSet<>();

    public Precio() {

    }

    public Precio(String moneda, BigDecimal monto) {
        this.moneda = moneda;
        this.monto = monto;
    }

    public Long getId() {
        return id;
    }

    public String getMoneda() {
        return moneda;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getEstado() {
        return estado;
    }

    public Set<AsignacionPrecio> getAsignacionesPrecio() {
        return asignacionesPrecio;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void addAsignacionPrecio(AsignacionPrecio asignacion) {
        this.asignacionesPrecio.add(asignacion);
        asignacion.setPrecio(this);
    }
}
