package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smail.sistema_mail_OdontoCool.entities.DetalleDiagnostico;
import smail.sistema_mail_OdontoCool.entities.Diagnostico;
import smail.sistema_mail_OdontoCool.entities.Diente;
import smail.sistema_mail_OdontoCool.repositories.DetalleDiagnosticoRepository;
import smail.sistema_mail_OdontoCool.repositories.DiagnosticoRepository;
import smail.sistema_mail_OdontoCool.repositories.DienteRepository;

@Service
public class DetalleDiagnosticoService {

    @Autowired
    private DetalleDiagnosticoRepository detalleDiagnosticoRepository;
    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private DiagnosticoRepository diagnosticoRepository;
    @Autowired
    private DienteRepository dienteRepository;

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

    private void insert(List<String> params, String fromEmail) {
        try {
            // observacion[0], zonaBucal[1], diagnosticoId[2], dienteId[3]

            if (params.size() < 4) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un detalle de diagnóstico.");
                return;
            }

            String observacion = params.get(0).trim();
            String zonaBucal = params.get(1).trim();
            String diagnosticoIdTexto = params.get(2).trim();
            String dienteIdTexto = params.get(3).trim();

            if (zonaBucal.isEmpty()) {
                sendResponse(fromEmail, "Error", "La zona bucal es obligatoria.");
                return;
            }

            if (diagnosticoIdTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del diagnóstico es obligatorio.");
                return;
            }

            if (dienteIdTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del diente es obligatorio.");
                return;
            }

            Long diagnosticoId = Long.parseLong(diagnosticoIdTexto);
            Long dienteId = Long.parseLong(dienteIdTexto);

            Diagnostico diagnostico = diagnosticoRepository.findById(diagnosticoId)
                    .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado con ID: " + diagnosticoId));

            Diente diente = dienteRepository.findById(dienteId)
                    .orElseThrow(() -> new RuntimeException("Diente no encontrado con ID: " + dienteId));

            DetalleDiagnostico detalle = new DetalleDiagnostico();
            detalle.setObservacion(observacion);
            detalle.setZonaBucal(zonaBucal);
            detalle.setDiagnostico(diagnostico);
            detalle.setDiente(diente);

            detalleDiagnosticoRepository.save(detalle);

            sendResponse(fromEmail, "Éxito",
                    "Detalle de diagnóstico registrado correctamente con ID: " + detalle.getId());

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del diagnóstico y el ID del diente deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar el detalle de diagnóstico: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Tratamientos.");
                }
            }
            sendResponse(fromEmail, "Listado de Detalles de Diagnóstico", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar detalles de diagnóstico: " + e.getMessage());
        }
    }

    private void update(List<String> params, String fromEmail) {
        try {
            // id[0], observacion[1], zonaBucal[2], diagnosticoId[3], dienteId[4]

            if (params.size() < 5) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un detalle de diagnóstico.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del detalle de diagnóstico es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            DetalleDiagnostico detalle = detalleDiagnosticoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Detalle de diagnóstico con ID: " + id + " no encontrado."));

            String observacion = params.get(1).trim();
            String zonaBucal = params.get(2).trim();
            String diagnosticoIdTexto = params.get(3).trim();
            String dienteIdTexto = params.get(4).trim();

            if (!observacion.isEmpty()) {
                detalle.setObservacion(observacion);
            }

            if (!zonaBucal.isEmpty()) {
                detalle.setZonaBucal(zonaBucal);
            }

            if (!diagnosticoIdTexto.isEmpty()) {
                Long diagnosticoId = Long.parseLong(diagnosticoIdTexto);

                Diagnostico diagnostico = diagnosticoRepository.findById(diagnosticoId)
                        .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado con ID: " + diagnosticoId));

                detalle.setDiagnostico(diagnostico);
            }

            if (!dienteIdTexto.isEmpty()) {
                Long dienteId = Long.parseLong(dienteIdTexto);

                Diente diente = dienteRepository.findById(dienteId)
                        .orElseThrow(() -> new RuntimeException("Diente no encontrado con ID: " + dienteId));

                detalle.setDiente(diente);
            }

            detalleDiagnosticoRepository.save(detalle);

            sendResponse(fromEmail, "Éxito",
                    "Detalle de diagnóstico con ID: " + detalle.getId() + " actualizado correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del detalle, diagnóstico y diente deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar detalle de diagnóstico: " + e.getMessage());
        }
    }

    // TODO: Implementar método delete para eliminar un detalle de diagnóstico por ID
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
        List<DetalleDiagnostico> detalles = detalleDiagnosticoRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Detalles de Diagnóstico\n");
        for (DetalleDiagnostico detalle : detalles) {
            sb.append(String.format("- [%s] Observación: %s Zona Bucal: %s Diente: %s\n",
                    detalle.getId(),
                    detalle.getObservacion(),
                    detalle.getZonaBucal(),
                    detalle.getDiente() != null ? detalle.getDiente().getId() : "N/A"));
        }
        return sb;
    }
}
