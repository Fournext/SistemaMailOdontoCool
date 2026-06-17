package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Diagnostico;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long> {

    boolean existsByCita_IdCita(Long citaId);

    @Query("""
           SELECT DISTINCT d
           FROM Diagnostico d
           LEFT JOIN FETCH d.detallesDiagnostico det
           LEFT JOIN FETCH det.diente
           LEFT JOIN FETCH d.cita
           """)
    List<Diagnostico> listarTodoConDetalles();

}
