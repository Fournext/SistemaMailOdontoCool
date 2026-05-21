package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Paciente;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private SmtpClientService smtpService;

    public void handle(String action, List<String> params, String fromEmail) {
        switch (action) {
            case "INS":
                insert(params, fromEmail);
                break;
            case "LIS":
                list(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Pacientes.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], ContactoEmerg[7], TelfEmerg[8]
            if (params.size() < 9) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Paciente. Se requieren 9.");
                return;
            }
            Paciente pac = new Paciente();
            pac.setCi(params.get(0));
            pac.setNombres(params.get(1));
            pac.setApellidos(params.get(2));
            pac.setDireccion(params.get(3));
            pac.setGenero(params.get(4));
            pac.setTelefono(params.get(5));
            pac.setFechaNacimiento(LocalDate.parse(params.get(6)));

            pac.setNombreContactoEmergencia(params.get(7));
            pac.setTelefonoEmergencia(params.get(8));
            pacienteRepository.save(pac);

            sendResponse(fromEmail, "Éxito", "Paciente " + pac.getNombres() + " registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar paciente: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            StringBuilder sb = new StringBuilder();
            if (params.size() == 0) {
                sendResponse(fromEmail, "Error",
                        "Falta especificar tipo de listado. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }
            if (params.size() == 1) {

                switch (params.get(0)) {
                    case "*":
                        sb = listAll();
                        break;
                    default:
                        sendResponse(fromEmail, "Error", "Listado no permitido para Pacientes.");
                }

            }
            sendResponse(fromEmail, "Listado de Pacientes", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar pacientes: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Paciente> lista = pacienteRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Pacientes:\n\n");
        for (Paciente p : lista) {
            sb.append(String.format("- [%s] %s %s %s %s %s (Emergencia: %s %s)\n",
                    p.getCi(),
                    p.getNombres(),
                    p.getApellidos(),
                    p.getDireccion(),
                    p.getTelefono(),
                    p.getFechaNacimiento(),
                    p.getTelefonoEmergencia(),
                    p.getNombreContactoEmergencia()));
        }
        return sb;
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en PacienteService: " + e.getMessage());
        }
    }
}
