package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuotas_multa")
@SQLDelete(sql = "UPDATE cuotas_multa SET estado = 'ELIMINADO' WHERE id_cuota_multa = ?")
@SQLRestriction("estado <> 'ELIMINADO'")
public class CuotaMulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_cuota_multa;

    @Column(name = "monto_multa")
    private Double montoMulta;

    @Column(name = "fecha_generada")
    private LocalDate fechaGenerada;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "estado")
    private String estado;

    @PrePersist
    public void setFechaGenerada() {
        if (this.fechaGenerada == null) {
            this.fechaGenerada = LocalDate.now();
        }
    }

    public CuotaMulta() {
    }

    // Getters y Setters
    public Long getId_cuota_multa() {
        return id_cuota_multa;
    }

    public Double getMontoMulta() {
        return montoMulta;
    }

    public void setMontoMulta(Double montoMulta) {
        this.montoMulta = montoMulta;
    }

    public LocalDate getFechaGenerada() {
        return fechaGenerada;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
