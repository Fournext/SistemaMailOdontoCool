package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.Especialidad;
import smail.sistema_mail_OdontoCool.repositories.EspecialidadRepository;

@Service
public class EspecialidadService {

    @Autowired
    private EspecialidadRepository especialidadRepository;

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
                sendResponse(fromEmail, "Error", "Acción no permitida en especialidades.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: Nombre[0], Estado[1]
            if (params.size() < 3) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Especialidad. Se requieren 3.");
                return;
            }

            Especialidad e = new Especialidad();
            e.setNombre(params.get(0));
            e.setDescripcion(params.get(1));
            e.setEstado(params.get(2));
            especialidadRepository.save(e);

            sendResponse(fromEmail, "Éxito", "Especialidad " + e.getNombre() + " registrada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar la especialidad. Detalles: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Doctores.");
                }

            }
            sendResponse(fromEmail, "Listado de Especialidades", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar Especialidades: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Especialidad> especialidades = especialidadRepository.findAll();
        StringBuilder sb = new StringBuilder("Listado de Especialidades:\n\n");

        for (Especialidad e : especialidades) {
            sb.append(String.format(
                    "- [%s] Nombre: %s, Descripción: %s, Estado: %s\n",
                    e.getId(),
                    e.getNombre(),
                    e.getDescripcion(),
                    e.getEstado()
            ));
        }

        return sb;
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

}
