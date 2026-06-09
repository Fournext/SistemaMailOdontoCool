package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.ResultadoAnalisis;

@Repository
public interface ResultadoAnalisisRepository extends JpaRepository<ResultadoAnalisis, Long> {

}
