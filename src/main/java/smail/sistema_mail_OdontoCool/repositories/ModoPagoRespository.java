package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.ModoPago;

@Repository
public interface ModoPagoRespository extends JpaRepository<ModoPago, Long> {
    boolean existsByNombre(String nombre);

    ModoPago findByNombre(String nombre);
}
