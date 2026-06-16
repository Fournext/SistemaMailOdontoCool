package smail.sistema_mail_OdontoCool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.ResultadoAnalisis;

@Repository
public interface ResultadoAnalisisRepository extends JpaRepository<ResultadoAnalisis, Long> {
    // Método para buscar resultado de análisis por ID de solicitud de análisis
     ResultadoAnalisis findBySolicitudAnalisisId(Long solicitudAnalisisId);

}
