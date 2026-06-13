package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Secretaria;

@Repository
public interface SecretariaRepository extends JpaRepository<Secretaria, String> {

    @Query("SELECT s FROM Secretaria s WHERE LOWER(s.persona.ci) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.persona.nombres) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.persona.apellidos) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Secretaria> searchByNameOrCi(@Param("query") String query);
}
