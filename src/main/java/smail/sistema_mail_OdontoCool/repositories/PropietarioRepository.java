package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import smail.sistema_mail_OdontoCool.entities.Propietario;

public interface PropietarioRepository extends JpaRepository<Propietario, String> {
    boolean existsByNombres(String nombres);
}
