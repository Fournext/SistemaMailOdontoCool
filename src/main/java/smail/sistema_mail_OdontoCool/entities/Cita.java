package smail.sistema_mail_OdontoCool.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;

    @Column(name = "fecha_cita")
    private LocalDate fechaCita;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDate fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "ci_secretaria", referencedColumnName = "ci", nullable = false)
    private Secretaria secretaria;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ci_paciente", referencedColumnName = "ci", nullable = false)
    private Paciente paciente;

    @OneToMany(mappedBy = "cita")
    private List<AsignacionEstadoCita> asignacionesEstadoCita;

    @ManyToOne(optional = true)
    @JoinColumn(name = "codigo_historial", referencedColumnName = "codigo_historial", nullable = true)
    private HistorialClinico historialClinico;

    @OneToOne(mappedBy = "cita")
    private SesionTratamiento sesionTratamiento;

    @OneToOne(mappedBy = "cita")
    private Diagnostico diagnostico;

    public Cita() {
    }

    // Getters y Setters
    public Long getIdCita() {
        return idCita;
    }

    public LocalDate getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(LocalDate fechaCita) {
        this.fechaCita = fechaCita;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setSecretaria(Secretaria secretaria) {
        this.secretaria = secretaria;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public HistorialClinico getHistorialClinico() {
        return historialClinico;
    }

    public void setHistorialClinico(HistorialClinico historialClinico) {
        this.historialClinico = historialClinico;
    }

    public SesionTratamiento getSesionTratamiento() {
        return sesionTratamiento;
    }

    public void setSesionTratamiento(SesionTratamiento sesionTratamiento) {
        this.sesionTratamiento = sesionTratamiento;
    }

    public List<AsignacionEstadoCita> getAsignacionesEstadoCita() {
        return asignacionesEstadoCita;
    }

    public Diagnostico getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(Diagnostico diagnostico) {
        this.diagnostico = diagnostico;
    }

}
