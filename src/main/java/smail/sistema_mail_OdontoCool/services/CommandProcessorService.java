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
    private HelpService helpService;

    @Autowired
    private SmtpClientService smtpService;

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
        Usuario user = usuarioRepository.findByCorreoElectronico(fromEmail);

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
