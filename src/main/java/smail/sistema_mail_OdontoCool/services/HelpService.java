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
                + "AYUDA - SISTEMA ODONTOCOOL (CORREO)\n"
                + "========================================\n\n"
                + "FORMATO GENERAL:\n"
                + "  COMANDO[\"param1\", \"param2\", ...]\n\n"
                + "1. DOCTORES (DOC):\n"
                + "   - INSDOC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"CodDoc\", \"Exp\", \"Matrícula\"]\n"
                + "   - LISDOC[] -> Listar doctores.\n\n"
                + "2. PACIENTES (PAC):\n"
                + "   - INSPAC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"CodPac\", \"ContactoEmerg\", \"TelfEmerg\"]\n"
                + "   - LISPAC[] -> Listar pacientes.\n\n"
                + "3. SECRETARIAS (SEC):\n"
                + "   - INSSEC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"CodSec\", \"FechaContratacion\"]\n"
                + "   - LISSEC[] -> Listar secretarias.\n\n"
                + "4. PROPIETARIOS (PRO):\n"
                + "   - INSPRO[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"FechaInicio\", \"Porcentaje\"]\n"
                + "   - LISPRO[] -> Listar propietarios.\n\n"
                + "========================================\n"
                + "NOTA: Las fechas deben ir en formato AAAA-MM-DD (Ej: 1995-05-15).\n"
                + "========================================\n";

        try {
            smtpService.sendEmail(to, "Manual de Comandos - OdontoCool", message);
        } catch (IOException e) {
            System.err.println("Error al enviar ayuda: " + e.getMessage());
        }
    }
}
