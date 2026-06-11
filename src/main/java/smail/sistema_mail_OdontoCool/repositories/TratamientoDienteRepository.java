package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.TratamientoDiente;

@Repository
public interface TratamientoDienteRepository extends JpaRepository<TratamientoDiente, Long> {

}
