package smail.sistema_mail_OdontoCool.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Modulo;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Long> {
    Optional<Modulo> findByNombreIgnoreCase(String nombre);
}
