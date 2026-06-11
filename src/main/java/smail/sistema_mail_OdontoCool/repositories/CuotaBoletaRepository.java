package smail.sistema_mail_OdontoCool.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.CuotaBoleta;
import smail.sistema_mail_OdontoCool.entities.BoletaPago;

@Repository
public interface CuotaBoletaRepository extends JpaRepository<CuotaBoleta, UUID> {
    CuotaBoleta findByNumeroCuotaAndBoletaPago(Integer numeroCuota, BoletaPago boletaPago);
}
