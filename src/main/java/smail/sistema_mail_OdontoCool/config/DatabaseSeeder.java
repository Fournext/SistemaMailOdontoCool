package smail.sistema_mail_OdontoCool.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.MetodoPago;
import smail.sistema_mail_OdontoCool.entities.ModoPago;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Propietario;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.entities.Rol;
import smail.sistema_mail_OdontoCool.entities.Modulo;
import smail.sistema_mail_OdontoCool.entities.AsignacionPermiso;
import smail.sistema_mail_OdontoCool.repositories.MetodoPagoRespository;
import smail.sistema_mail_OdontoCool.repositories.ModoPagoRespository;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.PropietarioRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.repositories.RolRepository;
import smail.sistema_mail_OdontoCool.repositories.ModuloRepository;
import smail.sistema_mail_OdontoCool.repositories.AsignacionPermisoRepository;
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

    @Autowired
    private MetodoPagoRespository metodoPagoRespository;

    @Autowired
    private ModoPagoRespository modoPagoRespository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private AsignacionPermisoRepository asignacionPermisoRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            System.out.println("====== SEEDING DESACTIVADO (app.seed.enabled=false) ======");
            return;
        }

        System.out.println("====== INICIANDO CARGA DE DATOS DE PRUEBA (SEEDING) ======");

        // 1. Roles
        Rol rolPropietario = saveRolIfNotExist("PROPIETARIO", "ACTIVO", "Dueño de la clínica con acceso total");
        Rol rolDoctor = saveRolIfNotExist("DOCTOR", "ACTIVO", "Médico dentista de la clínica");
        Rol rolSecretaria = saveRolIfNotExist("SECRETARIA", "ACTIVO", "Personal administrativo");
        Rol rolPaciente = saveRolIfNotExist("PACIENTE", "ACTIVO", "Paciente de la clínica");

        // 2. Módulos
        Modulo modUsuarios = saveModuloIfNotExist("Usuarios");
        Modulo modEspecialidades = saveModuloIfNotExist("Especialidades");
        Modulo modHistorial = saveModuloIfNotExist("Historial Clínico");
        Modulo modCitas = saveModuloIfNotExist("Citas");
        Modulo modTurnos = saveModuloIfNotExist("Turnos");
        Modulo modPagos = saveModuloIfNotExist("Pagos");
        Modulo modTratamientos = saveModuloIfNotExist("Tratamientos");
        Modulo modAnalisis = saveModuloIfNotExist("Análisis");
        Modulo modDiagnosticos = saveModuloIfNotExist("Diagnósticos");
        Modulo modRecetas = saveModuloIfNotExist("Recetas");

        // 3. Asignación de Permisos
        // PROPIETARIO
        savePermisoIfNotExist("Permiso Propietario Usuarios", rolPropietario, modUsuarios, null);
        savePermisoIfNotExist("Permiso Propietario Especialidades", rolPropietario, modEspecialidades, null);
        savePermisoIfNotExist("Permiso Propietario Historial", rolPropietario, modHistorial, null);
        savePermisoIfNotExist("Permiso Propietario Citas", rolPropietario, modCitas, null);
        savePermisoIfNotExist("Permiso Propietario Turnos", rolPropietario, modTurnos, null);
        savePermisoIfNotExist("Permiso Propietario Pagos", rolPropietario, modPagos, null);
        savePermisoIfNotExist("Permiso Propietario Tratamientos", rolPropietario, modTratamientos, null);
        savePermisoIfNotExist("Permiso Propietario Analisis", rolPropietario, modAnalisis, null);
        savePermisoIfNotExist("Permiso Propietario Diagnosticos", rolPropietario, modDiagnosticos, null);
        savePermisoIfNotExist("Permiso Propietario Recetas", rolPropietario, modRecetas, null);

        // DOCTOR
        savePermisoIfNotExist("Permiso Doctor Historial", rolDoctor, modHistorial, null);
        savePermisoIfNotExist("Permiso Doctor Tratamientos", rolDoctor, modTratamientos, null);
        savePermisoIfNotExist("Permiso Doctor Analisis", rolDoctor, modAnalisis, null);
        savePermisoIfNotExist("Permiso Doctor Diagnosticos", rolDoctor, modDiagnosticos, null);
        savePermisoIfNotExist("Permiso Doctor Recetas", rolDoctor, modRecetas, null);

        // SECRETARIA
        savePermisoIfNotExist("Permiso Secretaria Citas", rolSecretaria, modCitas, null);
        savePermisoIfNotExist("Permiso Secretaria Turnos", rolSecretaria, modTurnos, null);

        // PACIENTE
        savePermisoIfNotExist("Permiso Paciente Citas", rolPaciente, modCitas, null);
        savePermisoIfNotExist("Permiso Paciente Historial", rolPaciente, modHistorial, null);
        savePermisoIfNotExist("Permiso Paciente Tratamientos", rolPaciente, modTratamientos, null);
        savePermisoIfNotExist("Permiso Paciente Recetas", rolPaciente, modRecetas, null);

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
                usuario.setCorreoElectronico("sebastianzc041107@gmail.com");
                usuario.setContraseña(passwordService.hashPassword("admin123"));
                usuario.setEstado("ACTIVO");
                usuario.setFotoUrl(
                        "https://res.cloudinary.com/dmfl4ahiy/image/upload/v1779885975/imagenes_correo/imagen_correo.avif");
                usuario.setPersona(persona);
                usuario.setRol(rolPropietario);
                usuarioRepository.save(usuario);
            }
            System.out.println("====== SEED EXITOSO: Propietario Admin cargado (CI: 9999999) ======");
        } else {
            System.out.println("====== SEED: El propietario con CI 9999999 ya existe. Omitiendo... ======");
            // Si el usuario existe pero no tiene rol, se lo asignamos
            usuarioRepository.findByPersona_Ci(propietarioCi).ifPresent(usuario -> {
                if (usuario.getRol() == null) {
                    usuario.setRol(rolPropietario);
                    usuarioRepository.save(usuario);
                    System.out
                            .println("====== SEED: Rol PROPIETARIO asignado al usuario existente de CI 9999999 ======");
                }
            });
        }

        // Verificar si los metodos de pago ya existen
        if (!metodoPagoRespository.existsByNombre("Efectivo")) {
            MetodoPago metodoPago = new MetodoPago();
            metodoPago.setNombre("Efectivo");
            metodoPagoRespository.save(metodoPago);
        }

        if (!metodoPagoRespository.existsByNombre("QR")) {
            MetodoPago metodoPago = new MetodoPago();
            metodoPago.setNombre("QR");
            metodoPagoRespository.save(metodoPago);
        }

        System.out.println("====== SEED EXITOSO: Metodos de pago cargados ======");

        // Verificar si los modos de pago existen
        if (!modoPagoRespository.existsByNombre("Crédito")) {
            ModoPago modoPago = new ModoPago();
            modoPago.setNombre("Crédito");
            modoPagoRespository.save(modoPago);
        }

        if (!modoPagoRespository.existsByNombre("Contado")) {
            ModoPago modoPago = new ModoPago();
            modoPago.setNombre("Contado");
            modoPagoRespository.save(modoPago);
        }

        System.out.println("====== SEED EXITOSO: Modos de pago cargados ======");
    }

    private Rol saveRolIfNotExist(String nombre, String estado, String descripcion) {
        return rolRepository.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            Rol nuevo = new Rol();
            nuevo.setNombre(nombre);
            nuevo.setEstado(estado);
            nuevo.setDescripcion(descripcion);
            Rol guardado = rolRepository.save(nuevo);
            System.out.println("Seeded Rol: " + nombre);
            return guardado;
        });
    }

    private Modulo saveModuloIfNotExist(String nombre) {
        return moduloRepository.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            Modulo nuevo = new Modulo();
            nuevo.setNombre(nombre);
            Modulo guardado = moduloRepository.save(nuevo);
            System.out.println("Seeded Modulo: " + nombre);
            return guardado;
        });
    }

    private void savePermisoIfNotExist(String nombre, Rol rol, Modulo modulo, LocalDate fechaFinalizacion) {
        boolean existe = asignacionPermisoRepository.findAll().stream()
                .anyMatch(p -> p.getRol().getIdRol().equals(rol.getIdRol())
                        && p.getModulo().getIdModulo().equals(modulo.getIdModulo()));

        if (!existe) {
            AsignacionPermiso nuevo = new AsignacionPermiso();
            nuevo.setNombre(nombre);
            nuevo.setRol(rol);
            nuevo.setModulo(modulo);
            nuevo.setFechaFinalizacion(fechaFinalizacion);
            asignacionPermisoRepository.save(nuevo);
            System.out.println("Seeded AsignacionPermiso: " + nombre + " para " + rol.getNombre());
        }
    }
}
