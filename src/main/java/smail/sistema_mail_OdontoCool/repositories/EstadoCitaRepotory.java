package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.EstadoCita;

@Repository
public interface EstadoCitaRepotory extends JpaRepository<EstadoCita, Long> {
    EstadoCita findByNombre(String nombre);

}
