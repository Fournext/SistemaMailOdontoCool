package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String> {

    @Query("SELECT p FROM Paciente p WHERE LOWER(p.persona.ci) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.persona.nombres) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.persona.apellidos) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Paciente> searchByNameOrCi(@Param("query") String query);
}
