package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Cita;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByPaciente_Ci(String ci);

    @Query("SELECT DISTINCT c FROM Cita c LEFT JOIN FETCH c.asignacionesEstadoCita")
    List<Cita> findAllWithAsignaciones();

    @Query("SELECT DISTINCT c FROM Cita c LEFT JOIN FETCH c.asignacionesEstadoCita WHERE c.paciente.ci = :ci")
    List<Cita> findByPaciente_CiWithAsignaciones(@Param("ci") String ci);

    @Query("SELECT c FROM Cita c LEFT JOIN FETCH c.asignacionesEstadoCita WHERE c.idCita = :idCita")
    Optional<Cita> findByIdWithAsignaciones(@Param("idCita") Long idCita);

    @Query("SELECT DISTINCT c FROM Cita c LEFT JOIN FETCH c.asignacionesEstadoCita WHERE c.doctor.ci = :ci AND c.fechaCita = :fecha")
    List<Cita> findByDoctor_CiAndFechaCitaWithAsignaciones(@Param("ci") String ci, @Param("fecha") LocalDate fecha);
}
