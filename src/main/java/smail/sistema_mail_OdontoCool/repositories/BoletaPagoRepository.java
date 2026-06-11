package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.BoletaPago;

@Repository
public interface BoletaPagoRepository extends JpaRepository<BoletaPago, Long> {
    List<BoletaPago> findByPaciente_Ci(String ci);
}
