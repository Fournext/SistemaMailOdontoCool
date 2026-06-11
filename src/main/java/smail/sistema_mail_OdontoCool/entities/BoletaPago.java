package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "boleta_pago")
@SQLDelete(sql = "UPDATE boleta_pago SET estado_pago = 'ELIMINADO' WHERE id_boleta = ?")
@SQLRestriction("estado_pago <> 'ELIMINADO'")
public class BoletaPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boleta", nullable = false)
    private Long idBoleta;

    @Column(name = "descuento")
    private double descuento;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmicion;

    @Column(name = "total")
    private double total;

    @Column(name = "estado_pago")
    private String estadoPago;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ci_secretaria", referencedColumnName = "ci", nullable = false)
    private Secretaria secretaria;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ci_paciente", referencedColumnName = "ci", nullable = false)
    private Paciente paciente;

    @OneToMany(mappedBy = "boletaPago", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleBoleta> detallesBoleta;

    @OneToMany(mappedBy = "boletaPago", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CuotaBoleta> cuotasBoleta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_modo_pago", referencedColumnName = "id_modo_pago")
    private ModoPago modoPago;

    public BoletaPago() {
    }

    // Getters and Setters

    public Long getIdBoleta() {
        return idBoleta;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public LocalDate getFechaEmicion() {
        return fechaEmicion;
    }

    public void setFechaEmicion(LocalDate fechaEmicion) {
        this.fechaEmicion = fechaEmicion;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public void setSecretaria(Secretaria secretaria) {
        this.secretaria = secretaria;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public List<DetalleBoleta> getDetallesBoleta() {
        return detallesBoleta;
    }

    public void setDetallesBoleta(List<DetalleBoleta> detallesBoleta) {
        this.detallesBoleta = detallesBoleta;
    }

    public List<CuotaBoleta> getCuotasBoleta() {
        return cuotasBoleta;
    }

    public void setCuotasBoleta(List<CuotaBoleta> cuotasBoleta) {
        this.cuotasBoleta = cuotasBoleta;
    }

    public ModoPago getModoPago() {
        return modoPago;
    }

    public void setModoPago(ModoPago modoPago) {
        this.modoPago = modoPago;
    }

}
