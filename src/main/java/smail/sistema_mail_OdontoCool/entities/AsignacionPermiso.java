package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignacion_permisos")
public class AsignacionPermiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "fecha_agregacion")
    private LocalDate fechaAgregacion;

    @Column(name = "fecha_finalizacion")
    private LocalDate fechaFinalizacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_modulo", referencedColumnName = "id_modulo", nullable = false)
    private Modulo modulo;

    @PrePersist
    public void prePersist() {
        if (fechaAgregacion == null) {
            fechaAgregacion = LocalDate.now();
        }
    }

    public AsignacionPermiso() {
    }

    public Long getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Long idPermiso) {
        this.idPermiso = idPermiso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaAgregacion() {
        return fechaAgregacion;
    }

    public void setFechaAgregacion(LocalDate fechaAgregacion) {
        this.fechaAgregacion = fechaAgregacion;
    }

    public LocalDate getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDate fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }
}
