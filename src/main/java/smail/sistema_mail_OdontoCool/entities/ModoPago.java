package smail.sistema_mail_OdontoCool.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "modo_pago")
public class ModoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modo_pago")
    private Long idModoPago;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "estado")
    private String estado;

    @OneToMany(mappedBy = "modoPago", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoletaPago> boletasPago;

    public ModoPago() {
    }

    // Getter y Setters

    public Long getIdModoPago() {
        return idModoPago;
    }

    public void setIdModoPago(Long idModoPago) {
        this.idModoPago = idModoPago;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
