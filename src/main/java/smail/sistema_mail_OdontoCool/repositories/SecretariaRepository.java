package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import smail.sistema_mail_OdontoCool.entities.Secretarias;

public interface SecretariaRepository extends JpaRepository<Secretarias, String> {
}
