package smail.sistema_mail_OdontoCool.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.AsignacionPermiso;

@Repository
public interface AsignacionPermisoRepository extends JpaRepository<AsignacionPermiso, Long> {
    List<AsignacionPermiso> findByRol_IdRol(Long idRol);
    List<AsignacionPermiso> findByModulo_IdModulo(Long idModulo);
}
