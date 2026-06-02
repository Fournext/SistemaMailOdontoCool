package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Turno;
import smail.sistema_mail_OdontoCool.repositories.TurnoRepository;

@Service
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;
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
            case "MOD":
                update(params, fromEmail);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para Doctores.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            //Parametros: nombre[0], hora_inicio[1], hora_fin[2], estado[3]
            if (params.size() < 4) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para registrar un turno.");
                return;
            }

            Turno turno = new Turno();
            turno.setNombre(params.get(0));
            turno.setHoraInicio(params.get(1));
            turno.setHoraFin(params.get(2));
            turno.setEstado(params.get(3));
            turnoRepository.save(turno);
            sendResponse(fromEmail, "Éxito", "Turno: " + turno.getNombre() + " registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un turno: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            StringBuilder sb = new StringBuilder();
            if (params.size() == 0) {
                sendResponse(fromEmail, "Error", "Falta especificar tipo de listado. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }
            if (params.size() == 1) {
                switch (params.get(0)) {
                    case "*":
                        sb = listAll();
                        break;
                    default:
                        sendResponse(fromEmail, "Error", "Listado no permitido para Turnos.");
                }
            }
            sendResponse(fromEmail, "Listado de Turnos", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar turnos: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Turno> turnos = turnoRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Listado de Turnos:\n");
        for (Turno turno : turnos) {
            sb.append(String.format("- [%s] Turno. %s Hora-Inicio: %s Hora-Fin: %s Estado: %s\n",
                    turno.getId(),
                    turno.getNombre(),
                    turno.getHoraInicio(),
                    turno.getHoraFin(),
                    turno.getEstado()));
        }
        return sb;
    }

    private void update(List<String> params, String fromEmail) {
        try {
            //Parametros: id[0], nombre[1], hora_inicio[2], hora_fin[3], estado[4]
            // if (params.size() < 5) {
            //     sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un turno.");
            //     return;
            // }
            Long id = Long.parseLong(params.get(0));
            String nombre = params.get(1);
            String horaInicio = params.get(2);
            String horaFin = params.get(3);
            String estado = params.get(4);
            Turno turno = turnoRepository.findById(id).orElse(null);
            if (turno == null) {
                sendResponse(fromEmail, "Error", "Turno con ID: " + id + " no encontrado.");
                return;
            }
            turno.setNombre(nombre);
            turno.setHoraInicio(horaInicio);
            turno.setHoraFin(horaFin);
            turno.setEstado(estado);
            turnoRepository.save(turno);
            sendResponse(fromEmail, "Éxito", "Turno: " + turno.getNombre() + " actualizado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar turno: " + e.getMessage());
        }
    }

    private void delete(List<String> params, String fromEmail) {
        try {

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar turno: " + e.getMessage());
        }

    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

}
