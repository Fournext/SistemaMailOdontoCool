package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.SolicitudAnalisis;

@Repository
public interface SolicitudAnalisisRepository extends JpaRepository<SolicitudAnalisis, Long> {

    // Método para buscar solicitudes de análisis por ID de tratamiento
    List<SolicitudAnalisis> findByTratamientoId(Long tratamientoId);

    //metodo para buscar solicitudes de análisis por ID de solicitud de análisis
    //vamos a implementar un metodo para buscar una solicitud y traer sus resultados
    @Query("""
    SELECT sa
    FROM SolicitudAnalisis sa
    LEFT JOIN FETCH sa.analisis
    LEFT JOIN FETCH sa.resultadoAnalisis
    WHERE sa.tratamiento.id = :tratamientoId
""")
    List<SolicitudAnalisis> findByTratamientoIdConResultado(@Param("tratamientoId") Long tratamientoId);

}
