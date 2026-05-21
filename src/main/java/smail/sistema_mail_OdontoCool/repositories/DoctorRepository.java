package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import smail.sistema_mail_OdontoCool.entities.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, String> {
}
