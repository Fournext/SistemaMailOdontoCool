package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Analisis;
import smail.sistema_mail_OdontoCool.repositories.AnalisisRepository;

@Service
public class AnalisisService {

    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private AnalisisRepository analisisRepository;

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
                sendResponse(fromEmail, "Error", "Acción no permitida para Analisis.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // nombre[0], descripcion[1], estado[2]

            if (params.size() < 3) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un análisis.");
                return;
            }

            String nombre = params.get(0).trim();
            String descripcion = params.get(1).trim();
            String estado = params.get(2).trim();

            if (nombre.isEmpty()) {
                sendResponse(fromEmail, "Error", "El nombre del análisis es obligatorio.");
                return;
            }

            if (estado.isEmpty()) {
                estado = "ACTIVO";
            }

            if (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("INACTIVO")) {
                sendResponse(fromEmail, "Error", "Estado inválido. Use ACTIVO o INACTIVO.");
                return;
            }

            if (analisisRepository.existsByNombreIgnoreCase(nombre)) {
                sendResponse(fromEmail, "Error", "Ya existe un análisis con el nombre: " + nombre);
                return;
            }

            Analisis analisis = new Analisis();
            analisis.setNombre(nombre);
            analisis.setDescripcion(descripcion);
            analisis.setEstado(estado.toUpperCase());

            analisisRepository.save(analisis);

            sendResponse(fromEmail, "Éxito", "Análisis registrado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un análisis: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para análisis.");
                }
            }
            sendResponse(fromEmail, "Listado de Análisis", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar análisis: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Parámetros: id[0], nombre[1], descripcion[2], estado[3]

            if (params.size() < 4) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un análisis.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del análisis es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            Analisis analisis = analisisRepository.findById(id).orElse(null);

            if (analisis == null) {
                sendResponse(fromEmail, "Error", "Análisis con ID: " + id + " no encontrado.");
                return;
            }

            String nombre = params.get(1).trim();
            String descripcion = params.get(2).trim();
            String estado = params.get(3).trim();

            if (!nombre.isEmpty()) {
                if (!analisis.getNombre().equalsIgnoreCase(nombre)
                        && analisisRepository.existsByNombreIgnoreCase(nombre)) {
                    sendResponse(fromEmail, "Error", "Ya existe un análisis con el nombre: " + nombre);
                    return;
                }

                analisis.setNombre(nombre);
            }

            if (!descripcion.isEmpty()) {
                analisis.setDescripcion(descripcion);
            }

            if (!estado.isEmpty()) {
                if (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("INACTIVO")) {
                    sendResponse(fromEmail, "Error", "Estado inválido. Use ACTIVO o INACTIVO.");
                    return;
                }

                analisis.setEstado(estado.toUpperCase());
            }

            analisisRepository.save(analisis);

            sendResponse(fromEmail, "Éxito", "Análisis con ID: " + analisis.getId() + " actualizado correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del análisis debe ser un valor numérico.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar análisis: " + e.getMessage());
        }
    }

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
        List<Analisis> analisis = analisisRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Análisis\n");
        for (Analisis a : analisis) {
            sb.append(String.format("- [%s] Nombre: %s Descripción: %s Estado: %s\n",
                    a.getId(),
                    a.getNombre(),
                    a.getDescripcion(),
                    a.getEstado()));
        }
        return sb;
    }

}
