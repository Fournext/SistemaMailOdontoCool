package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommandProcessorService {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private HelpService helpService;

    private static final Pattern COMMAND_PATTERN = Pattern.compile("^([A-Z]+)(?:\\[(.*)\\])?$");

    public void process(String subject, String fromEmail) {
        try {
            Matcher matcher = COMMAND_PATTERN.matcher(subject.trim());

            if (matcher.matches()) {
                String fullCommand = matcher.group(1);
                String paramsRaw = (matcher.group(2) != null) ? matcher.group(2) : "";
                List<String> params = parseParams(paramsRaw);

                routeToEntityService(fullCommand, params, fromEmail);
            } else {
                sendResponse(fromEmail, "Error de Formato",
                        "El formato del asunto es inválido. Recibido: [" + subject + "]\n" +
                                "Formato esperado: COMANDO[\"param1\", \"param2\"]");
            }
        } catch (Exception e) {
            sendResponse(fromEmail, "Error Crítico", "Error al procesar el mensaje: " + e.getMessage());
        }
    }

    private void routeToEntityService(String fullCommand, List<String> params, String fromEmail) {
        if (fullCommand.length() < 3) {
            sendResponse(fromEmail, "Error", "Comando no reconocido.");
            return;
        }

        // Preguntar si es un comando especial
        if (fullCommand.equals("HELP")) {
            helpService.sendHelp(fromEmail);
            return;
        }

        String action = fullCommand.substring(0, 3); // INS, LIS, MOD, DEL
        String entity = fullCommand.substring(3); // PER, SRV, CTA

        switch (entity) {
            case "PER":
                personaService.handle(action, params, fromEmail);
                break;
            // case "SRV": srvService.handle(action, params, fromEmail); break;
            default:
                sendResponse(fromEmail, "Error", "Entidad no reconocida: " + entity);
        }
    }

    private List<String> parseParams(String paramsRaw) {
        List<String> params = new ArrayList<>();
        if (paramsRaw.isEmpty())
            return params;
        String[] split = paramsRaw.split("\",\"");
        for (String s : split)
            params.add(s.replace("\"", "").trim());
        return params;
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en Router: " + e.getMessage());
        }
    }
}
