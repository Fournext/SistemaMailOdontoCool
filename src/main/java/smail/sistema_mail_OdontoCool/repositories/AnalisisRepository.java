package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Analisis;

@Repository
public interface AnalisisRepository extends JpaRepository<Analisis, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
