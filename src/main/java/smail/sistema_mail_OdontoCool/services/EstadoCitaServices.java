package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.AsignacionEstadoCita;
import smail.sistema_mail_OdontoCool.entities.Cita;
import smail.sistema_mail_OdontoCool.entities.EstadoCita;
import smail.sistema_mail_OdontoCool.repositories.AsignacionEstadoCitaRepository;
import smail.sistema_mail_OdontoCool.repositories.CitaRepository;
import smail.sistema_mail_OdontoCool.repositories.EstadoCitaRepotory;
import smail.sistema_mail_OdontoCool.validations.EstadoCitaVal;

@Service
public class EstadoCitaServices {

    @Autowired
    private EstadoCitaRepotory estadoCitaRepotory;

    @Autowired
    private EstadoCitaVal estadoCitaVal;

    @Autowired
    private CitaRepository citaRepocitory;

    @Autowired
    private AsignacionEstadoCitaRepository asignacionEstadoCitaRepository;

    @Autowired
    private SmtpClientService smtpService;

    public void handle(String actions, List<String> params, String fromEmail) {
        switch (actions) {
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
            case "ASE":
                asignarEstado(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Estado de Citas.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parametros: Nombre[0], Descripcion[1]
            if (params.size() < 2) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Estado de Cita. Se requieren 2.");
                return;
            }

            String validacion = estadoCitaVal.insertValid(params);

            if (!validacion.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validacion);
                return;
            }

            EstadoCita estadoCita = new EstadoCita();
            estadoCita.setNombre(params.get(0));
            estadoCita.setDescripcion(params.get(1));
            estadoCitaRepotory.save(estadoCita);
            sendResponse(fromEmail, "Éxito", "Estado de Cita " + params.get(0) + " registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar Estado de Cita: " + e.getMessage());
        }
    }

    @Transactional
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Estado de Citas.");
                }
            }
            sendResponse(fromEmail, "Listado de Estado de Citas", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar Estado de Citas: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<EstadoCita> lista = estadoCitaRepotory.findAll();
        StringBuilder sb = new StringBuilder("Lista de Estado de Citas:\n\n");
        for (EstadoCita e : lista) {
            sb.append(String.format("- [%s] %s %s\n",
                    e.getIdEstadoCita(),
                    e.getNombre(),
                    e.getDescripcion()));
        }
        return sb;
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {

            String validacion = estadoCitaVal.updateValid(params);

            if (!validacion.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validacion);
                return;
            }

            Long idEstadoCita = Long.valueOf(params.get(0));
            String nombre = params.get(1);
            String descripcion = params.get(2);
            EstadoCita estadoCita = estadoCitaRepotory.findById(idEstadoCita).orElse(null);
            if (estadoCita == null) {
                sendResponse(fromEmail, "Error", "Estado de Cita no encontrado.");
                return;
            }
            estadoCita.setNombre(nombre);
            estadoCita.setDescripcion(descripcion);
            estadoCitaRepotory.save(estadoCita);
            sendResponse(fromEmail, "Éxito", "Estado de Cita " + nombre + " modificado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al modificar Estado de Cita: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            Long idEstadoCita = Long.valueOf(params.get(0));
            EstadoCita estadoCita = estadoCitaRepotory.findById(idEstadoCita).orElse(null);
            if (estadoCita == null) {
                sendResponse(fromEmail, "Error", "Estado de Cita no encontrado.");
                return;
            }
            estadoCitaRepotory.delete(estadoCita);
            sendResponse(fromEmail, "Éxito", "Estado de Cita " + idEstadoCita + " eliminado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar Estado de Cita: " + e.getMessage());
        }
    }

    private void asignarEstado(List<String> params, String fromEmail) {
        try {
            // Parametros: IdCita[0], NombreEstado[1], Observaciones
            if (params.size() < 3) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para asignar Estado de Cita. Se requieren 2.");
                return;
            }

            Long idCita = Long.valueOf(params.get(0));
            String nombreEstado = params.get(1);
            Cita cita = citaRepocitory.findById(idCita).orElse(null);
            EstadoCita estadoCita = estadoCitaRepotory.findByNombre(nombreEstado);
            if (cita == null) {
                sendResponse(fromEmail, "Error", "Cita no encontrada.");
                return;
            }
            if (estadoCita == null) {
                sendResponse(fromEmail, "Error", "Estado de Cita no encontrado.");
                return;
            }
            AsignacionEstadoCita asignacionEstadoCita = new AsignacionEstadoCita();
            asignacionEstadoCita.setCita(cita);
            asignacionEstadoCita.setEstadoCita(estadoCita);
            asignacionEstadoCita.setObservaciones(params.get(2));
            asignacionEstadoCitaRepository.save(asignacionEstadoCita);
            sendResponse(fromEmail, "Éxito", "Estado de Cita " + estadoCita.getNombre() + " asignado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al asignar Estado de Cita: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en EstadoCitaService: " + e.getMessage());
        }
    }
}
