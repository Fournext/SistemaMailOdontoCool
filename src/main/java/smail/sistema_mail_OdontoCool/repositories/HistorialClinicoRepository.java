package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.HistorialClinico;

@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, String> {
    HistorialClinico findByCodigoHistorial(String codigoHistorial);

    boolean existsByPaciente_Ci(String ciPaciente);
}
