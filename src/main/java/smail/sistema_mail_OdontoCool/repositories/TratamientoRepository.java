package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Tratamiento;

@Repository
public interface TratamientoRepository extends JpaRepository<Tratamiento, Long> {
    //vamos a listar los tratamientos por historial clinico, entonces necesitamos un metodo que busque por el id del historial clinico
    List<Tratamiento> findByHistorialClinicoCodigoHistorial(String codigoHistorial);

    public boolean existsByDiagnosticoId(long parseLong);

}
