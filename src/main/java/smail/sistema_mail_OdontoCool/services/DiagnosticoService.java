package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Cita;
import smail.sistema_mail_OdontoCool.entities.Diagnostico;
import smail.sistema_mail_OdontoCool.repositories.CitaRepository;
import smail.sistema_mail_OdontoCool.repositories.DiagnosticoRepository;

@Service

public class DiagnosticoService {

    @Autowired
    private DiagnosticoRepository diagnosticoRepository;
    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private CitaRepository citaRepository;

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
                sendResponse(fromEmail, "Error", "Acción no permitida para Diagnosticos.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // sintomas[0], tipoDiagnostico[1], gravedad[2], observaciones[3], citaId[4]

            if (params.size() < 5) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un diagnóstico.");
                return;
            }

            String sintomas = params.get(0).trim();
            String tipoDiagnostico = params.get(1).trim();
            String gravedad = params.get(2).trim();
            String observaciones = params.get(3).trim();
            String citaIdTexto = params.get(4).trim();

            if (sintomas.isEmpty()) {
                sendResponse(fromEmail, "Error", "Los síntomas son obligatorios.");
                return;
            }

            if (tipoDiagnostico.isEmpty()) {
                sendResponse(fromEmail, "Error", "El tipo de diagnóstico es obligatorio.");
                return;
            }

            if (gravedad.isEmpty()) {
                sendResponse(fromEmail, "Error", "La gravedad es obligatoria.");
                return;
            }

            if (citaIdTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID de la cita es obligatorio.");
                return;
            }

            Long citaId = Long.parseLong(citaIdTexto);

            Cita cita = citaRepository.findById(citaId)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + citaId));

            if (diagnosticoRepository.existsByCita_IdCita(citaId)) {
                sendResponse(fromEmail, "Error", "Esta cita ya tiene un diagnóstico registrado.");
                return;
            }

            Diagnostico diagnostico = new Diagnostico();
            diagnostico.setSintomas(sintomas);
            diagnostico.setTipoDiagnostico(tipoDiagnostico);
            diagnostico.setGravedad(gravedad);
            diagnostico.setObservaciones(observaciones);
            diagnostico.setCita(cita);

            diagnosticoRepository.save(diagnostico);

            sendResponse(fromEmail, "Éxito", "Diagnóstico registrado correctamente con ID: " + diagnostico.getId());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar el diagnóstico: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Diagnosticos.");
                }
            }
            sendResponse(fromEmail, "Listado de Diagnosticos", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar diagnosticos: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // id[0], sintomas[1], tipoDiagnostico[2], gravedad[3], observaciones[4], citaId[5]

            if (params.size() < 6) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un diagnóstico.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del diagnóstico es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            Diagnostico diagnostico = diagnosticoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Diagnóstico con ID: " + id + " no encontrado."));

            String sintomas = params.get(1).trim();
            String tipoDiagnostico = params.get(2).trim();
            String gravedad = params.get(3).trim();
            String observaciones = params.get(4).trim();
            String citaIdTexto = params.get(5).trim();

            if (!sintomas.isEmpty()) {
                diagnostico.setSintomas(sintomas);
            }

            if (!tipoDiagnostico.isEmpty()) {
                diagnostico.setTipoDiagnostico(tipoDiagnostico);
            }

            if (!gravedad.isEmpty()) {
                diagnostico.setGravedad(gravedad);
            }

            if (!observaciones.isEmpty()) {
                diagnostico.setObservaciones(observaciones);
            }

            if (!citaIdTexto.isEmpty()) {
                Long citaId = Long.parseLong(citaIdTexto);

                Cita cita = citaRepository.findById(citaId)
                        .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + citaId));

                if (diagnosticoRepository.existsByCita_IdCita(citaId)
                        && !diagnostico.getCita().getIdCita().equals(citaId)) {
                    sendResponse(fromEmail, "Error", "Esta cita ya tiene otro diagnóstico registrado.");
                    return;
                }

                diagnostico.setCita(cita);
            }

            diagnosticoRepository.save(diagnostico);

            sendResponse(fromEmail, "Éxito", "Diagnóstico con ID: " + diagnostico.getId() + " actualizado correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del diagnóstico y el ID de la cita deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar diagnóstico: " + e.getMessage());
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
        List<Diagnostico> diagnosticos = diagnosticoRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Diagnosticos\n");
        for (Diagnostico diagnostico : diagnosticos) {
            sb.append(String.format("- [%s] sintomas: %s tipoDiagnostico: %s, gravedad: %s, observaciones: %s, cita: %s\n",
                    diagnostico.getId(),
                    diagnostico.getSintomas(),
                    diagnostico.getTipoDiagnostico(),
                    diagnostico.getGravedad(),
                    diagnostico.getObservaciones(),
                    diagnostico.getCita().getIdCita()));
        }
        return sb;
    }

}
