package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.RecetaRecomendacion;

@Repository
public interface RecetaRecomendacionRepository extends JpaRepository<RecetaRecomendacion, Long> {

    List<RecetaRecomendacion> findByTratamientoId(Long tratamientoId);

    @Query("""
           SELECT DISTINCT r
           FROM RecetaRecomendacion r
           LEFT JOIN FETCH r.detallesRecomendacion
           """)
    List<RecetaRecomendacion> listarTodoConDetalles();

    @Query("""
           SELECT DISTINCT r
           FROM RecetaRecomendacion r
           LEFT JOIN FETCH r.detallesRecomendacion
           WHERE r.tratamiento.id = :tratamientoId
           """)
    List<RecetaRecomendacion> listarPorTratamientoConDetalles(@Param("tratamientoId") Long tratamientoId);
}
