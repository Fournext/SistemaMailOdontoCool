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
                + "   - INSDOC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"Exp\", \"Matrícula\"]\n"
                + "         Ejemplo: INSDOC[\"12345678\", \"Juan\", \"Perez\", \"Calle Falsa 123\", \"M\", \"555-1234\", \"1980-01-01\", \"10 años\", \"MAT-001\"]\n"
                + "   - LISDOC[*] -> Listar todos los doctores.\n\n"
                + "2. PACIENTES (PAC):\n"
                + "   - INSPAC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"ContactoEmerg\", \"TelfEmerg\"]\n"
                + "        Ejemplo: INSPAC[\"87654321\", \"Maria\", \"Gomez\", \"Avenida Siempre Viva 456\", \"F\", \"555-5678\", \"1990-02-02\", \"Carlos Gomez\", \"555-8765\"]\n"
                + "   - LISPAC[*] -> Listar todos los pacientes.\n\n"
                + "3. SECRETARIAS (SEC):\n"
                + "   - INSSEC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"FechaContratacion\"]\n"
                + "        Ejemplo: INSSEC[\"11223344\", \"Ana\", \"Lopez\", \"Calle Luna 789\", \"F\", \"555-4321\", \"1985-03-03\", \"2010-05-01\"]\n"
                + "   - LISSEC[*] -> Listar todas las secretarias.\n\n"
                + "4. PROPIETARIOS (PRO):\n"
                + "   - INSPRO[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"FechaInicio\", \"Porcentaje\"]\n"
                + "        Ejemplo: INSPRO[\"55667788\", \"Luis\", \"Martinez\", \"Avenida Sol 321\", \"M\", \"555-6789\", \"1975-04-04\", \"2005-06-01\", \"25\"]\n"
                + "         (El porcentaje es un número decimal que representa la participación del propietario en la clínica, por ejemplo, 25 para un 25% de participación)\n"
                + "   - LISPRO[*] -> Listar todos los propietarios.\n\n"
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
