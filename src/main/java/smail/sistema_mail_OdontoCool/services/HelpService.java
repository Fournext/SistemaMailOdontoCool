package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class HelpService {

    @Autowired
    private SmtpClientService smtpService;

    public void sendHelp(String to) {
        String message = "========================================\n"
                + "AYUDA - SISTEMA DE CORREO ODONTOCOOL\n"
                + "========================================\n\n"
                + "FORMATO DE COMANDO:\n"
                + "  COMANDO[\"param1\", \"param2\", ...]\n\n"
                + "COMANDOS DISPONIBLES:\n\n"
                + "1. GESTIÓN DE PERSONAS (PER):\n"
                + "   - INSPER[\"CI\", \"Nombre\", \"Apellido\", \"Profesión\", \"Telf\", \"Cel\", \"Email\"]\n"
                + "     Ejemplo: INSPER[\"123\", \"Juan\", \"Perez\", \"Doc\", \"333\", \"777\", \"j@m.com\"]\n\n"
                + "   - LISPER[\"*\"] -> Lista todas las personas.\n"
                + "   - LISPER[\"CI\"] -> Muestra una persona específica por CI.\n\n"
                + "2. AYUDA:\n"
                + "   - HELP -> Recibir este mensaje de ayuda.\n\n"
                + "========================================\n"
                + "NOTAS:\n"
                + "  - Los parámetros deben ir entre comillas dobles y separados por coma.\n"
                + "  - El comando debe ir en el ASUNTO del correo.\n"
                + "========================================\n";

        try {
            smtpService.sendEmail(to, "Guía de Comandos - OdontoCool", message);
        } catch (IOException e) {
            System.err.println("Error al enviar ayuda: " + e.getMessage());
        }
    }
}
