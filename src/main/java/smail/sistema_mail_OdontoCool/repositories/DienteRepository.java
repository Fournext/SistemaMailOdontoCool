package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Diente;

@Repository
public interface DienteRepository extends JpaRepository<Diente, Long> {

    boolean existsByNumero(Integer numero);
}
