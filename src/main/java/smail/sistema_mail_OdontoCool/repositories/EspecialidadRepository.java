package smail.sistema_mail_OdontoCool.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Especialidad;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, UUID> {

}
