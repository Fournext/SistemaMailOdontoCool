package smail.sistema_mail_OdontoCool.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.Analisis;
import smail.sistema_mail_OdontoCool.entities.AsignacionPrecio;
import smail.sistema_mail_OdontoCool.entities.Diente;
import smail.sistema_mail_OdontoCool.entities.MetodoPago;
import smail.sistema_mail_OdontoCool.entities.ModoPago;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Precio;
import smail.sistema_mail_OdontoCool.entities.Propietario;
import smail.sistema_mail_OdontoCool.entities.Servicio;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.entities.Rol;
import smail.sistema_mail_OdontoCool.entities.Modulo;
import smail.sistema_mail_OdontoCool.entities.AsignacionPermiso;
import smail.sistema_mail_OdontoCool.repositories.AnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.AsignacionPrecioRepository;
import smail.sistema_mail_OdontoCool.repositories.DienteRepository;
import smail.sistema_mail_OdontoCool.repositories.MetodoPagoRespository;
import smail.sistema_mail_OdontoCool.repositories.ModoPagoRespository;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.PrecioRepository;
import smail.sistema_mail_OdontoCool.repositories.PropietarioRepository;
import smail.sistema_mail_OdontoCool.repositories.ServicioRepository;
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
        private DienteRepository dienteRepository;

        @Autowired
        private AnalisisRepository analisisRepository;

        @Autowired
        private ServicioRepository servicioRepository;

        @Autowired
        private PrecioRepository precioRepository;

        @Autowired
        private AsignacionPrecioRepository asignacionPrecioRepository;

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
                // dientes

                System.out.println("====== SEED EXITOSO: Modos de pago cargados ======");
                seedDientes();
                // analisis
                seedAnalisis();
                // servicios con precios
                seedServiciosConPrecios();
        }
        // seed para cargar el catalogo de dientes, se verifica si el diente con el
        // numero ya existe para evitar duplicados

        private void crearDiente(String nombre, Integer numero, String tipo, String ubicacion) {
                if (!dienteRepository.existsByNumero(numero)) {
                        Diente diente = new Diente();
                        diente.setNombre(nombre);
                        diente.setNumero(numero);
                        diente.setTipo(tipo);
                        diente.setUbicacion(ubicacion);
                        diente.setEstado("ACTIVO");

                        dienteRepository.save(diente);
                }
        }

        private void seedDientes() {
                crearDiente("Incisivo central superior derecho", 11, "Incisivo", "Superior derecho");
                crearDiente("Incisivo lateral superior derecho", 12, "Incisivo", "Superior derecho");
                crearDiente("Canino superior derecho", 13, "Canino", "Superior derecho");
                crearDiente("Primer premolar superior derecho", 14, "Premolar", "Superior derecho");
                crearDiente("Segundo premolar superior derecho", 15, "Premolar", "Superior derecho");
                crearDiente("Primer molar superior derecho", 16, "Molar", "Superior derecho");
                crearDiente("Segundo molar superior derecho", 17, "Molar", "Superior derecho");
                crearDiente("Tercer molar superior derecho", 18, "Molar", "Superior derecho");

                crearDiente("Incisivo central superior izquierdo", 21, "Incisivo", "Superior izquierdo");
                crearDiente("Incisivo lateral superior izquierdo", 22, "Incisivo", "Superior izquierdo");
                crearDiente("Canino superior izquierdo", 23, "Canino", "Superior izquierdo");
                crearDiente("Primer premolar superior izquierdo", 24, "Premolar", "Superior izquierdo");
                crearDiente("Segundo premolar superior izquierdo", 25, "Premolar", "Superior izquierdo");
                crearDiente("Primer molar superior izquierdo", 26, "Molar", "Superior izquierdo");
                crearDiente("Segundo molar superior izquierdo", 27, "Molar", "Superior izquierdo");
                crearDiente("Tercer molar superior izquierdo", 28, "Molar", "Superior izquierdo");

                crearDiente("Incisivo central inferior izquierdo", 31, "Incisivo", "Inferior izquierdo");
                crearDiente("Incisivo lateral inferior izquierdo", 32, "Incisivo", "Inferior izquierdo");
                crearDiente("Canino inferior izquierdo", 33, "Canino", "Inferior izquierdo");
                crearDiente("Primer premolar inferior izquierdo", 34, "Premolar", "Inferior izquierdo");
                crearDiente("Segundo premolar inferior izquierdo", 35, "Premolar", "Inferior izquierdo");
                crearDiente("Primer molar inferior izquierdo", 36, "Molar", "Inferior izquierdo");
                crearDiente("Segundo molar inferior izquierdo", 37, "Molar", "Inferior izquierdo");
                crearDiente("Tercer molar inferior izquierdo", 38, "Molar", "Inferior izquierdo");

                crearDiente("Incisivo central inferior derecho", 41, "Incisivo", "Inferior derecho");
                crearDiente("Incisivo lateral inferior derecho", 42, "Incisivo", "Inferior derecho");
                crearDiente("Canino inferior derecho", 43, "Canino", "Inferior derecho");
                crearDiente("Primer premolar inferior derecho", 44, "Premolar", "Inferior derecho");
                crearDiente("Segundo premolar inferior derecho", 45, "Premolar", "Inferior derecho");
                crearDiente("Primer molar inferior derecho", 46, "Molar", "Inferior derecho");
                crearDiente("Segundo molar inferior derecho", 47, "Molar", "Inferior derecho");
                crearDiente("Tercer molar inferior derecho", 48, "Molar", "Inferior derecho");

                System.out.println("====== SEED EXITOSO: Catálogo de dientes cargado ======");
        }
        // analisis, se verifica si el analisis con el nombre ya existe para evitar
        // duplicados

        private void crearAnalisis(String nombre, String descripcion) {

                if (!analisisRepository.existsByNombre(nombre)) {

                        Analisis analisis = new Analisis();
                        analisis.setNombre(nombre);
                        analisis.setDescripcion(descripcion);
                        analisis.setEstado("ACTIVO");

                        analisisRepository.save(analisis);
                }
        }

        private void seedAnalisis() {

                crearAnalisis("Radiografía Periapical",
                                "Radiografía intraoral para evaluar uno o varios dientes específicos.");

                crearAnalisis("Radiografía Panorámica",
                                "Radiografía completa de maxilares, dientes y estructuras adyacentes.");

                crearAnalisis("Radiografía Cefalométrica",
                                "Radiografía lateral del cráneo utilizada en ortodoncia.");

                crearAnalisis("Tomografía Cone Beam (CBCT)",
                                "Tomografía computarizada tridimensional para diagnóstico odontológico avanzado.");

                crearAnalisis("Hemograma Completo",
                                "Análisis sanguíneo para evaluar el estado general del paciente.");

                crearAnalisis("Glucemia",
                                "Medición de glucosa en sangre para pacientes diabéticos o con sospecha.");

                crearAnalisis("Tiempo de Protrombina (TP)",
                                "Evaluación de la coagulación sanguínea antes de procedimientos quirúrgicos.");

                crearAnalisis("Tiempo Parcial de Tromboplastina (TPT)",
                                "Evaluación complementaria de la coagulación sanguínea.");

                crearAnalisis("Prueba de VIH",
                                "Análisis solicitado cuando el historial clínico lo requiera.");

                crearAnalisis("Prueba de Hepatitis B",
                                "Evaluación previa a determinados procedimientos odontológicos.");

                crearAnalisis("Prueba de Hepatitis C",
                                "Evaluación complementaria en pacientes con factores de riesgo.");

                crearAnalisis("Cultivo Bacteriano",
                                "Identificación microbiológica de infecciones orales.");

                crearAnalisis("Biopsia Oral",
                                "Estudio histopatológico de lesiones sospechosas.");

                crearAnalisis("Prueba de Sensibilidad Pulpar",
                                "Evaluación de la vitalidad de la pulpa dental.");

                crearAnalisis("Estudio Periodontal",
                                "Evaluación diagnóstica del estado de encías y tejidos de soporte.");

                System.out.println("====== SEED EXITOSO: Catálogo de análisis cargado ======");
        }

        private void seedServiciosConPrecios() {
                crearServicioConPrecio("Consulta odontológica general",
                                "Evaluación inicial del estado bucal del paciente.",
                                "Consulta", "BOB", "80");
                crearServicioConPrecio("Limpieza dental", "Profilaxis dental para remover placa bacteriana y sarro.",
                                "Preventivo", "BOB", "150");
                crearServicioConPrecio("Aplicación de flúor",
                                "Tratamiento preventivo para fortalecer el esmalte dental.",
                                "Preventivo", "BOB", "100");
                crearServicioConPrecio("Sellantes dentales", "Aplicación de sellantes para prevenir caries.",
                                "Preventivo",
                                "BOB", "120");
                crearServicioConPrecio("Restauración dental", "Curación de caries con resina u otro material.",
                                "Operatoria",
                                "BOB", "180");
                crearServicioConPrecio("Extracción dental simple",
                                "Extracción de una pieza dental sin cirugía compleja.",
                                "Cirugía", "BOB", "200");
                crearServicioConPrecio("Extracción de tercer molar", "Extracción de muela del juicio.", "Cirugía",
                                "BOB",
                                "450");
                crearServicioConPrecio("Endodoncia", "Tratamiento de conductos radiculares.", "Endodoncia", "BOB",
                                "500");
                crearServicioConPrecio("Blanqueamiento dental", "Procedimiento estético para aclarar dientes.",
                                "Estética",
                                "BOB", "700");
                crearServicioConPrecio("Ortodoncia", "Tratamiento para corregir la posición dental.", "Ortodoncia",
                                "BOB",
                                "2500");
                crearServicioConPrecio("Control de ortodoncia", "Revisión y ajuste periódico de ortodoncia.",
                                "Ortodoncia",
                                "BOB", "150");
                crearServicioConPrecio("Periodoncia", "Tratamiento de encías y tejidos de soporte dental.",
                                "Periodoncia",
                                "BOB", "300");
                crearServicioConPrecio("Raspado y alisado radicular", "Limpieza profunda debajo de la encía.",
                                "Periodoncia",
                                "BOB", "350");
                crearServicioConPrecio("Prótesis dental", "Rehabilitación mediante prótesis removible o fija.",
                                "Rehabilitación", "BOB", "1200");
                crearServicioConPrecio("Corona dental", "Restauración fija que cubre una pieza dental dañada.",
                                "Rehabilitación", "BOB", "900");
                crearServicioConPrecio("Implante dental", "Reemplazo de una pieza dental mediante implante.",
                                "Implantología",
                                "BOB", "5000");
                crearServicioConPrecio("Radiografía dental", "Estudio radiográfico para diagnóstico odontológico.",
                                "Diagnóstico", "BOB", "100");
                crearServicioConPrecio("Urgencia odontológica",
                                "Atención inmediata por dolor, inflamación o traumatismo dental.", "Urgencia", "BOB",
                                "120");

                System.out.println("====== SEED EXITOSO: Servicios con precios cargados ======");
        }

        private void crearServicioConPrecio(String nombre, String descripcion, String tipo, String moneda,
                        String montoTexto) {
                Servicio servicio = servicioRepository.findByNombre(nombre).orElseGet(() -> {
                        Servicio nuevo = new Servicio();
                        nuevo.setNombre(nombre);
                        nuevo.setDescripcion(descripcion);
                        nuevo.setTipo(tipo);
                        nuevo.setEstado("ACTIVO");
                        return servicioRepository.save(nuevo);
                });

                BigDecimal monto = new BigDecimal(montoTexto);

                List<AsignacionPrecio> asignacionesActivas = asignacionPrecioRepository
                                .findByServicioIdAndEstado(servicio.getId(), "ACTIVO");

                boolean yaTieneMismoPrecioActivo = asignacionesActivas.stream()
                                .anyMatch(ap -> ap.getPrecio().getMonto().compareTo(monto) == 0
                                                && ap.getPrecio().getMoneda().equalsIgnoreCase(moneda));

                if (yaTieneMismoPrecioActivo) {
                        return;
                }

                for (AsignacionPrecio asignacionActiva : asignacionesActivas) {
                        asignacionActiva.setEstado("INACTIVO");
                        asignacionActiva.setFechaFin(LocalDate.now().minusDays(1));
                }

                asignacionPrecioRepository.saveAll(asignacionesActivas);

                Precio precio = precioRepository.findByMonto(monto).orElseGet(() -> {
                        Precio nuevoPrecio = new Precio();
                        nuevoPrecio.setMoneda(moneda);
                        nuevoPrecio.setMonto(monto);
                        nuevoPrecio.setEstado("ACTIVO");
                        return precioRepository.save(nuevoPrecio);
                });

                AsignacionPrecio nuevaAsignacion = new AsignacionPrecio();
                nuevaAsignacion.setServicio(servicio);
                nuevaAsignacion.setPrecio(precio);
                nuevaAsignacion.setFechaInicio(LocalDate.now());
                nuevaAsignacion.setFechaFin(null);
                nuevaAsignacion.setEstado("ACTIVO");

                asignacionPrecioRepository.save(nuevaAsignacion);
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
