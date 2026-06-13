package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Propietario;

@Repository
public interface PropietarioRepository extends JpaRepository<Propietario, String> {
    boolean existsByNombres(String nombres);

    @Query("SELECT p FROM Propietario p WHERE LOWER(p.persona.ci) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.persona.nombres) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.persona.apellidos) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Propietario> searchByNameOrCi(@Param("query") String query);
}
