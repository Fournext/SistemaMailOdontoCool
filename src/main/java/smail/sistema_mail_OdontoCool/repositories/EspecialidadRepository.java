package smail.sistema_mail_OdontoCool.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import smail.sistema_mail_OdontoCool.entities.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, UUID> {

}
