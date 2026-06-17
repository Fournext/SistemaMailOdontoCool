package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.ServicioPrestado;

@Repository
public interface ServicioPrestadoRepository extends JpaRepository<ServicioPrestado, Long> {

    List<ServicioPrestado> findByTratamientoId(Long tratamientoId);

}
