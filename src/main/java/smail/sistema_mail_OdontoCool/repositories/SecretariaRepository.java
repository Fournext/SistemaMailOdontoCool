package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Secretaria;

@Repository
public interface SecretariaRepository extends JpaRepository<Secretaria, String> {
}
