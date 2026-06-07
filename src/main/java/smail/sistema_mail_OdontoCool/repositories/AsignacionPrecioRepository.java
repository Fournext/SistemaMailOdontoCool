package smail.sistema_mail_OdontoCool.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.AsignacionPrecio;

@Repository
public interface AsignacionPrecioRepository extends JpaRepository<AsignacionPrecio, Long> {

    Optional<AsignacionPrecio> findTopByServicioIdAndEstadoOrderByFechaInicioDesc(
            Long servicioId,
            String estado
    );
}
