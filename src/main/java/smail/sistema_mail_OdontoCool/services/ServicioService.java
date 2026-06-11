package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Servicio;
import smail.sistema_mail_OdontoCool.repositories.ServicioRepository;

@Service
public class ServicioService {

    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private ServicioRepository servicioRepository;

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
            //Parametros: nombre[0], descripcion[1], estado[2]
            if (params.size() < 3) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para registrar un servicio.");
                return;
            }

            Servicio servicio = new Servicio();
            servicio.setNombre(params.get(0));
            servicio.setDescripcion(params.get(1));
            servicio.setEstado(params.get(2));
            servicioRepository.save(servicio);
            sendResponse(fromEmail, "Éxito", "Servicio: " + servicio.getNombre() + " registrado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un servicio: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            StringBuilder sb = new StringBuilder();
            if (params.size() < 1) {
                sendResponse(fromEmail, "Error", "Falta especificar tipo de listado. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }

            if (params.size() == 1) {
                switch (params.get(0)) {
                    case "*":
                        sb = listAll();
                        break;
                    default:
                        sendResponse(fromEmail, "Error", "Listado no permitido para Servicios.");
                }
            }
            sendResponse(fromEmail, "Listado de Servicios", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar servicios: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            //Parametros: id[0], nombre[1], Descripcion[2], estado[3]
            // if (params.size() < 4) {
            //     sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un servicio.");
            //     return;
            // }
            Long id = Long.parseLong(params.get(0));
            String nombre = params.get(1);
            String descripcion = params.get(2);
            String estado = params.get(3);
            Servicio servicio = servicioRepository.findById(id).orElse(null);
            if (servicio == null) {
                sendResponse(fromEmail, "Error", "Servicio con ID: " + id + " no encontrado.");
                return;
            }
            if (nombre != null && !nombre.trim().isEmpty()) {
                servicio.setNombre(nombre);
            }
            if (descripcion != null && !descripcion.trim().isEmpty()) {
                servicio.setDescripcion(descripcion);
            }
            if (estado != null && !estado.trim().isEmpty()) {
                servicio.setEstado(estado);
            }
            servicioRepository.save(servicio);
            sendResponse(fromEmail, "Éxito", "Servicio: " + servicio.getNombre() + " actualizado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar servicio: " + e.getMessage());
        }
    }

    // TODO: Implementar eliminación lógica (cambiar estado a INACTIVO) en lugar de eliminación física
    private void delete(List<String> params, String fromEmail) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Servicio> servicios = servicioRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Listado de Servicios:\n");
        for (Servicio servicio : servicios) {
            sb.append(String.format("- [%s] Servicio. %s Descripción: %s Estado: %s\n",
                    servicio.getId(),
                    servicio.getNombre(),
                    servicio.getDescripcion(),
                    servicio.getEstado()));
        }
        return sb;
    }

}
