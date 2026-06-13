package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String> {

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.persona.ci) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.persona.nombres) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.persona.apellidos) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Doctor> searchByNameOrCi(@Param("query") String query);
}
