package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import smail.sistema_mail_OdontoCool.entities.AsignacionTurnoDoctor;

import java.util.List;

public interface AsignacionTurnoDoctorRepository extends JpaRepository<AsignacionTurnoDoctor, Long> {
    List<AsignacionTurnoDoctor> findByDoctor_Ci(String doctorCi);
}
