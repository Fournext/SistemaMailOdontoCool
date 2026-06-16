package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.TratamientoDiente;

@Repository
public interface TratamientoDienteRepository extends JpaRepository<TratamientoDiente, Long> {

    public List<TratamientoDiente> findByTratamientoId(Long tratamientoId);

}
