package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuota_boleta")
public class CuotaBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id_cuota;

    @Column(name = "numero_cuota")
    private int numeroCuota;

    @Column(name = "monto_cuota")
    private Double montoCuota;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "estado")
    private String estado;

    @Column(name = "id_transaccion")
    private Long id_transaccion;

    @Column(name = "comprobante")
    private String comprobante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_boleta", referencedColumnName = "id_boleta")
    private BoletaPago boletaPago;

    @ManyToOne(optional = true)
    @JoinColumn(name = "id_metodo_pago", referencedColumnName = "id_metodo_pago", nullable = true)
    private MetodoPago metodoPago;

    @OneToOne(optional = true)
    @JoinColumn(name = "id_cuota_multa", referencedColumnName = "id_cuota_multa", nullable = true, unique = true)
    private CuotaMulta cuotaMulta;

    public CuotaBoleta() {
    }

    // Getter y setters

    public UUID getIdCuota() {
        return id_cuota;
    }

    public int getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(int numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public Double getMontoCuota() {
        return montoCuota;
    }

    public void setMontoCuota(Double montoCuota) {
        this.montoCuota = montoCuota;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public CuotaMulta getCuotaMulta() {
        return cuotaMulta;
    }

    public void setCuotaMulta(CuotaMulta cuotaMulta) {
        this.cuotaMulta = cuotaMulta;
    }

    public BoletaPago getBoletaPago() {
        return boletaPago;
    }

    public void setBoletaPago(BoletaPago boletaPago) {
        this.boletaPago = boletaPago;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public Long getId_transaccion() {
        return id_transaccion;
    }

    public void setId_transaccion(Long id_transaccion) {
        this.id_transaccion = id_transaccion;
    }

}
