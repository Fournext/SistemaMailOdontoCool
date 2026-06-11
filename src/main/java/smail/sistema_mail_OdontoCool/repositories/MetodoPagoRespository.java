package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.MetodoPago;

@Repository
public interface MetodoPagoRespository extends JpaRepository<MetodoPago, Long> {
    boolean existsByNombre(String nombre);
    MetodoPago findByNombre(String nombre);
}
