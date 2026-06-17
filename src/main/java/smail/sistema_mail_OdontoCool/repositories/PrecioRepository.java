package smail.sistema_mail_OdontoCool.repositories;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Precio;

@Repository
public interface PrecioRepository extends JpaRepository<Precio, Long> {

    // El PrecioRepository es una interfaz que extiende JpaRepository, lo que proporciona métodos CRUD para la entidad Precio. 
    // Además, se pueden definir métodos personalizados para consultas específicas relacionadas con los precios de los servicios odontológicos.
    Optional<Precio> findByMonto(BigDecimal monto);
}
