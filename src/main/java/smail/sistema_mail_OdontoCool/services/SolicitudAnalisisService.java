package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Analisis;
import smail.sistema_mail_OdontoCool.entities.SolicitudAnalisis;
import smail.sistema_mail_OdontoCool.repositories.AnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.SolicitudAnalisisRepository;

@Service
public class SolicitudAnalisisService {

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private SolicitudAnalisisRepository solicitudAnalisisRepository;

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
                sendResponse(fromEmail, "Error", "Acción no permitida para Solicitud de Análisis.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // fechaSolicitud[0], motivo[1], estado[2], analisisId[3]

            if (params.size() < 4) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar una solicitud de análisis.");
                return;
            }

            String fechaTexto = params.get(0).trim();
            String motivo = params.get(1).trim();
            String estado = params.get(2).trim();
            String analisisIdTexto = params.get(3).trim();

            if (fechaTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "La fecha de solicitud es obligatoria.");
                return;
            }

            if (motivo.isEmpty()) {
                sendResponse(fromEmail, "Error", "El motivo de la solicitud es obligatorio.");
                return;
            }

            if (estado.isEmpty()) {
                estado = "ACTIVO";
            }

            if (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("INACTIVO")) {
                sendResponse(fromEmail, "Error", "Estado inválido. Use ACTIVO o INACTIVO.");
                return;
            }

            if (analisisIdTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del análisis es obligatorio.");
                return;
            }

            Long analisisId = Long.parseLong(analisisIdTexto);

            Analisis analisis = analisisRepository.findById(analisisId)
                    .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + analisisId));

            SolicitudAnalisis solicitud = new SolicitudAnalisis();
            solicitud.setFechaSolicitud(LocalDate.parse(fechaTexto));
            solicitud.setMotivo(motivo);
            solicitud.setEstado(estado.toUpperCase());
            solicitud.setAnalisis(analisis);

            solicitudAnalisisRepository.save(solicitud);

            sendResponse(fromEmail, "Éxito", "Solicitud de análisis registrada correctamente con ID: " + solicitud.getId());

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del análisis debe ser numérico.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar la solicitud de análisis: " + e.getMessage());
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
            // id[0], fechaSolicitud[1], motivo[2], estado[3], analisisId[4]

            if (params.size() < 5) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar una solicitud de análisis.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID de la solicitud de análisis es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            SolicitudAnalisis solicitud = solicitudAnalisisRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud de análisis con ID: " + id + " no encontrada."));

            String fechaTexto = params.get(1).trim();
            String motivo = params.get(2).trim();
            String estado = params.get(3).trim();
            String analisisIdTexto = params.get(4).trim();

            if (!fechaTexto.isEmpty()) {
                solicitud.setFechaSolicitud(LocalDate.parse(fechaTexto));
            }

            if (!motivo.isEmpty()) {
                solicitud.setMotivo(motivo);
            }

            if (!estado.isEmpty()) {
                if (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("INACTIVO")) {
                    sendResponse(fromEmail, "Error", "Estado inválido. Use ACTIVO o INACTIVO.");
                    return;
                }

                solicitud.setEstado(estado.toUpperCase());
            }

            if (!analisisIdTexto.isEmpty()) {
                Long analisisId = Long.parseLong(analisisIdTexto);

                Analisis analisis = analisisRepository.findById(analisisId)
                        .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + analisisId));

                solicitud.setAnalisis(analisis);
            }

            solicitudAnalisisRepository.save(solicitud);

            sendResponse(fromEmail, "Éxito",
                    "Solicitud de análisis con ID: " + solicitud.getId() + " actualizada correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID de la solicitud y el ID del análisis deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar solicitud de análisis: " + e.getMessage());
        }
    }
// TODO: Implementar delete con eliminación lógica (cambiar estado a INACTIVO)

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
        List<SolicitudAnalisis> solicitudes = solicitudAnalisisRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Solicitudes de Análisis\n");
        for (SolicitudAnalisis s : solicitudes) {
            sb.append(String.format("- [%s] Fecha: %s Motivo: %s Estado: %s Análisis: %s\n",
                    s.getId(),
                    s.getFechaSolicitud(),
                    s.getMotivo(),
                    s.getEstado(),
                    s.getAnalisis() != null ? s.getAnalisis().getNombre() : "N/A"));
        }
        return sb;
    }

}
