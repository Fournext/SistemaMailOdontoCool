package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

@Service
public class CommandProcessorService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private SecretariaService secretariaService;

    @Autowired
    private PropietarioService propietarioService;

    @Autowired
    private EspecialidadService especialidadService;

    @Autowired
    private HistorialClinicoService historialClinicoService;

    @Autowired
    private CitaServices citaServices;

    @Autowired
    private EstadoCitaServices estadoCitaServices;

    @Autowired
    private AntecedenteOdontologicoServices antecedenteOdontologicoServices;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private AsignacionTurnoDoctorService AsignacionTurnoDoctorService;

    @Autowired
    private AsignacionTurnoSecretariaService AsignacionTurnoSecretariaService;

    @Autowired
    private HelpService helpService;

    @Autowired
    private BoletaPagoService boletaPagoService;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private ServicioService ServicioService;

    @Autowired
    private PrecioService PrecioService;
    @Autowired
    private AsignacionPrecioService AsignacionPrecioService;
    @Autowired
    private ServicioPrestadoService ServicioPrestadoService;
    @Autowired
    private TratamientoDienteService TratamientoDienteService;
    @Autowired
    private TratamientoService TratamientoService;
    @Autowired
    private DienteService DienteService;
    @Autowired
    private AnalisisService AnalisisService;
    @Autowired
    private SolicitudAnalisisService SolicitudAnalisisService;
    @Autowired
    private ResultadoAnalisisService ResultadoAnalisisService;
    @Autowired
    private DiagnosticoService diagnosticoService;
    @Autowired
    private DetalleDiagnosticoService DetalleDiagnosticoService;
    @Autowired
    private DetalleRecomendacionService DetalleRecomendacionService;
    @Autowired
    private RecetaRecomendacionService RecetaRecomendacionService;

    @Autowired
    private RolService rolService;

    @Autowired
    private ModuloService moduloService;

    @Autowired
    private AsignacionPermisoService asignacionPermisoService;
    @Autowired
    private ReporteEstadisticaService reporteEstadisticaService;

    // Regex mejorada: permite espacios entre el comando y los corchetes
    private static final Pattern COMMAND_PATTERN = Pattern.compile(
            "^\\s*([A-Z]+)\\s*(?:\\[\\s*(.*)\\s*\\])?\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public void process(String subject, String fromEmail, List<String> imagenesBase64) {
        String cleanSubject = subject.trim();

        try {
            Matcher matcher = COMMAND_PATTERN.matcher(cleanSubject);

            if (matcher.matches()) {
                String fullCommand = matcher.group(1).toUpperCase();
                String paramsRaw = (matcher.group(2) != null) ? matcher.group(2) : "";
                List<String> params = parseParams(paramsRaw);

                if (!existUserSistem(fromEmail)) {
                    helpService.sendHelp(fromEmail, "Usuario no registrado en el sistema.");
                    return;
                }

                routeToEntityService(fullCommand, params, fromEmail, imagenesBase64);
            } else {
                sendResponse(fromEmail, "Error de Formato",
                        "El formato del asunto es inválido.\n"
                                + "Recibido: [" + cleanSubject + "]\n"
                                + "Asegúrate de que no tenga prefijos como 'Re:' o 'Fwd:'");
            }
        } catch (Exception e) {
            sendResponse(fromEmail, "Error Crítico", "Error al procesar: " + e.getMessage());
        }
    }

    private void routeToEntityService(String fullCommand, List<String> params, String fromEmail,
            List<String> imagenesBase64) {
        if (fullCommand.equals("HELP")) {
            helpService.sendHelp(fromEmail, "");
            return;
        }

        if (fullCommand.length() < 3) {
            sendResponse(fromEmail, "Error", "Comando demasiado corto.");
            return;
        }

        String action = fullCommand.substring(0, 3); // INS, LIS, MOD, DEL
        String entity = fullCommand.substring(3); // DOC, PAC, SEC, PRO

        switch (entity) {
            case "DOC":
                doctorService.handle(action, params, fromEmail, imagenesBase64);
                break;
            case "PAC":
                pacienteService.handle(action, params, fromEmail, imagenesBase64);
                break;
            case "SEC":
                secretariaService.handle(action, params, fromEmail, imagenesBase64);
                break;
            case "PRO":
                propietarioService.handle(action, params, fromEmail, imagenesBase64);
                break;
            case "ESP":
                especialidadService.handle(action, params, fromEmail);
                break;
            case "HIS":
                historialClinicoService.handle(action, params, fromEmail);
                break;
            case "CIT":
                citaServices.handle(action, params, fromEmail);
                break;
            case "ECI":
                estadoCitaServices.handle(action, params, fromEmail);
                break;
            case "AOD":
                antecedenteOdontologicoServices.handle(action, params, fromEmail);
                break;
            case "TUR":
                // El servicio de Turnos no maneja imágenes, por lo que se pasa una lista vacía
                turnoService.handle(action, params, fromEmail);
                break;
            case "ATD":
                // El servicio de Asignación de Turnos a Doctores no maneja imágenes, por lo que
                // se pasa una lista vacía
                AsignacionTurnoDoctorService.handle(action, params, fromEmail);
                break;
            case "ATS":
                // El servicio de Asignación de Turnos a Secretarias no maneja imágenes, por lo
                // que se pasa una lista vacía
                AsignacionTurnoSecretariaService.handle(action, params, fromEmail);
                break;
            case "BPA":
                boletaPagoService.handle(action, params, fromEmail, imagenesBase64);
                break;
            case "SER":
                // El servicio de Servicios no maneja imágenes, por lo que se pasa una lista
                // vacía
                ServicioService.handle(action, params, fromEmail);
                break;
            case "PRE":
                // El servicio de Precios no maneja imágenes, por lo que se pasa una lista vacía
                PrecioService.handle(action, params, fromEmail);
                break;
            case "APS":
                // El servicio de Asignación de Precios a Servicios no maneja imágenes, por lo
                // que
                // se pasa una lista vacía
                AsignacionPrecioService.handle(action, params, fromEmail);
                break;
            case "SEP":
                // Servicios prestados no maneja imágenes, por lo que se pasa una lista vacía
                ServicioPrestadoService.handle(action, params, fromEmail);
                break;
            case "TRA":
                // El servicio de Tratamientos no maneja imágenes, por lo que se pasa una lista
                // vacía
                TratamientoService.handle(action, params, fromEmail);
                break;
            case "DNT":
                // El servicio de Dientes no maneja imágenes, por lo que se pasa una lista vacía
                DienteService.handle(action, params, fromEmail);
                break;
            case "TDI":
                // El servicio de Tratamiento de Dientes no maneja imágenes, por lo que se pasa
                // una
                // lista vacía
                TratamientoDienteService.handle(action, params, fromEmail);
                break;
            case "ANA":
                // El servicio de Análisis no maneja imágenes, por lo que se pasa una lista
                // vacía
                AnalisisService.handle(action, params, fromEmail);
                break;
            case "SOA":
                // El servicio de Solicitud de Análisis no maneja imágenes, por lo que se pasa
                // una lista vacía
                SolicitudAnalisisService.handle(action, params, fromEmail);
                break;
            case "RAN":
                // El servicio de Resultados de Análisis no maneja imágenes, por lo que se pasa
                // una lista vacía
                ResultadoAnalisisService.handle(action, params, fromEmail);
                break;
            case "DIA":
                // El servicio de Diagnósticos no maneja imágenes, por lo que se pasa una lista
                // vacía
                diagnosticoService.handle(action, params, fromEmail);
                break;
            case "DDI":
                // El servicio de Detalles de Diagnóstico no maneja imágenes, por lo que se pasa
                // una lista vacía
                DetalleDiagnosticoService.handle(action, params, fromEmail);
                break;
            case "REC":
                // El servicio de Recetas/Recomendaciones no maneja imágenes, por lo que se pasa
                // una lista vacía
                RecetaRecomendacionService.handle(action, params, fromEmail);
                break;
            case "DRE":
                // El servicio de Detalles de Recomendación no maneja imágenes, por lo que se
                // pasa una lista vacía
                DetalleRecomendacionService.handle(action, params, fromEmail);
                break;
            case "ROL":
                rolService.handle(action, params, fromEmail);
                break;
            case "MOD":
                moduloService.handle(action, params, fromEmail);
                break;
            case "PER":
                asignacionPermisoService.handle(action, params, fromEmail);
                break;
            case "REP":
                reporteEstadisticaService.handle(action, params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Entidad no reconocida: " + entity);

        }
    }

    private List<String> parseParams(String paramsRaw) {
        List<String> params = new ArrayList<>();
        if (paramsRaw.trim().isEmpty()) {
            return params;
        }

        // Split por coma rodeada de comillas, permitiendo espacios
        String[] split = paramsRaw.split("\",\\s*\"");
        for (String s : split) {
            params.add(s.replace("\"", "").trim());
        }
        return params;
    }

    private boolean existUserSistem(String fromEmail) {
        Usuario user = usuarioRepository.findFirstByCorreoElectronico(fromEmail);

        if (user == null) {
            return false;
        }
        return true;
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en Router: " + e.getMessage());
        }
    }
}
