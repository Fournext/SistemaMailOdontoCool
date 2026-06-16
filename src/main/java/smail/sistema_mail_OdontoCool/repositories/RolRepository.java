package smail.sistema_mail_OdontoCool.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombreIgnoreCase(String nombre);
}
