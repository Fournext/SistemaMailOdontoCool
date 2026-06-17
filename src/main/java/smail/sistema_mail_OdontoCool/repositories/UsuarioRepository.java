package smail.sistema_mail_OdontoCool.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    public Usuario findByCodigoUsuario(String codigoUsuario);

    public Usuario findFirstByCorreoElectronico(String correoElectronico);

    Optional<Usuario> findByPersona_Ci(String ci);

    @Query("SELECT u FROM Usuario u WHERE u.persona.ci = :ci AND u.codigoUsuario LIKE %:suffix")
    Optional<Usuario> findByPersonaCiAndSuffix(@Param("ci") String ci, @Param("suffix") String suffix);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.correoElectronico = :email AND LOWER(u.rol.nombre) = LOWER(:rolName)")
    boolean existsByCorreoElectronicoAndRolNombre(@Param("email") String email, @Param("rolName") String rolName);
}
