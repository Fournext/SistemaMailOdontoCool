package smail.sistema_mail_OdontoCool.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Propietario;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.PropietarioRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.services.PasswordService;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordService passwordService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            System.out.println("====== SEEDING DESACTIVADO (app.seed.enabled=false) ======");
            return;
        }

        System.out.println("====== INICIANDO CARGA DE DATOS DE PRUEBA (SEEDING) ======");

        String propietarioCi = "9999999";

        // Verificar si el propietario ya existe
        if (!propietarioRepository.existsById(propietarioCi)) {
            Persona persona = personaRepository.findById(propietarioCi).orElse(null);
            if (persona == null) {
                persona = new Persona();
                persona.setCi(propietarioCi);
                persona.setNombres("Admin");
                persona.setApellidos("Propietario");
                persona.setDireccion("Clinica OdontoCool");
                persona.setGenero("M");
                persona.setTelefono("77777777");
                persona.setFechaNacimiento(LocalDate.of(1980, 1, 1));
                persona = personaRepository.save(persona);
            }

            Propietario propietario = new Propietario();
            propietario.setPersona(persona);
            propietario.setPorcentajeParticipacion(new BigDecimal("100.0"));
            propietarioRepository.save(propietario);

            // Crear usuario para el propietario si no existe
            String codUsuario = "P" + propietarioCi + "PRP";
            if (usuarioRepository.findByPersona_Ci(propietarioCi).isEmpty()) {
                Usuario usuario = new Usuario();
                usuario.setCodigoUsuario(codUsuario);
                usuario.setCorreoElectronico("biofarmfarmaciaquimica@gmail.com");
                usuario.setContraseña(passwordService.hashPassword("admin123"));
                usuario.setEstado("ACTIVO");
                usuario.setFotoUrl(
                        "https://res.cloudinary.com/dmfl4ahiy/image/upload/v1779885975/imagenes_correo/imagen_correo.avif");
                usuario.setPersona(persona);
                usuarioRepository.save(usuario);
            }
            System.out.println("====== SEED EXITOSO: Propietario Admin cargado (CI: 9999999) ======");
        } else {
            System.out.println("====== SEED: El propietario con CI 9999999 ya existe. Omitiendo... ======");
        }
    }
}
