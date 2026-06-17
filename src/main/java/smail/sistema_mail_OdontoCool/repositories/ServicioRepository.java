package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Servicio;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    // El ServicioRepository es una interfaz que extiende JpaRepository, lo que proporciona métodos CRUD para la entidad Servicio. 
    // Además, se pueden definir métodos personalizados para consultas específicas relacionadas con los servicios odontológicos.
    Optional<Servicio> findByNombre(String nombre);

    @Query("""
    SELECT DISTINCT s
    FROM Servicio s
    LEFT JOIN FETCH s.asignacionesPrecio ap
    LEFT JOIN FETCH ap.precio
    """)
    List<Servicio> findAllConPrecios();

    @Query("""
    SELECT DISTINCT s
    FROM Servicio s
    LEFT JOIN FETCH s.asignacionesPrecio ap
    LEFT JOIN FETCH ap.precio
    WHERE LOWER(s.nombre) = LOWER(:nombre)
    """)
    Optional<Servicio> findByNombreConPrecios(String nombre);

    Optional<Servicio> findByTipoIgnoreCase(String tipo);
}
