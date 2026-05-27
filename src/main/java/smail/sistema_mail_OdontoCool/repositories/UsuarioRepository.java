package smail.sistema_mail_OdontoCool.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smail.sistema_mail_OdontoCool.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    public Usuario findByCodigoUsuario(String codigoUsuario);

    public Usuario findByCorreoElectronico(String correoElectronico);

    Optional<Usuario> findByPersona_Ci(String ci);
}
