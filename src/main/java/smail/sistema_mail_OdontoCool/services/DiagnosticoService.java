package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Cita;
import smail.sistema_mail_OdontoCool.entities.DetalleDiagnostico;
import smail.sistema_mail_OdontoCool.entities.Diagnostico;
import smail.sistema_mail_OdontoCool.entities.Diente;
import smail.sistema_mail_OdontoCool.repositories.CitaRepository;
import smail.sistema_mail_OdontoCool.repositories.DiagnosticoRepository;
import smail.sistema_mail_OdontoCool.repositories.DienteRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.validations.DiagnosticoVal;

@Service

public class DiagnosticoService {

    @Autowired
    private DiagnosticoRepository diagnosticoRepository;
    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private DienteRepository dienteRepository;
    @Autowired
    private DiagnosticoVal diagnosticoVal;
    @Autowired
    private UsuarioRepository usuarioRepository;

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
        String errores = diagnosticoVal.insertValid(params);
        if (!errores.isEmpty()) {
            sendResponse(fromEmail, "Error", errores);
            return;
        }
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            /*
             * Formato:
             * INSDIA["Sintomas", "TipoDiagnostico", "Gravedad", "Observaciones", "ID Cita",
             * "ObservacionDetalle;ZonaBucal;ID Diente | ObservacionDetalle;ZonaBucal;ID Diente"
             * ]
             */

            if (params.size() < 6) {
                sendResponse(fromEmail, "Error",
                        "Parámetros insuficientes.\n"
                                + "Formato correcto:\n"
                                + "INSDIA[\"Sintomas\", \"TipoDiagnostico\", \"Gravedad\", \"Observaciones\", \"ID Cita\", \"ObservacionDetalle;ZonaBucal;ID Diente | ObservacionDetalle;ZonaBucal;ID Diente\"]");
                return;
            }

            String sintomas = params.get(0).trim();
            String tipoDiagnostico = params.get(1).trim();
            String gravedad = params.get(2).trim();
            String observaciones = params.get(3).trim();
            String citaIdTexto = params.get(4).trim();
            String detallesRaw = params.get(5).trim();

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

            if (detallesRaw.isEmpty()) {
                sendResponse(fromEmail, "Error", "Debe enviar al menos un detalle de diagnóstico.");
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

            cargarDetallesDesdeTexto(diagnostico, detallesRaw);

            if (diagnostico.getDetallesDiagnostico().isEmpty()) {
                sendResponse(fromEmail, "Error", "Debe registrar al menos un detalle válido.");
                return;
            }

            diagnosticoRepository.save(diagnostico);

            sendResponse(fromEmail, "Éxito",
                    "Diagnóstico registrado correctamente.\n"
                            + "ID Diagnóstico: " + diagnostico.getId() + "\n"
                            + "Detalles registrados: " + diagnostico.getDetallesDiagnostico().size());

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID de la cita y el ID del diente deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar el diagnóstico: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        // Verificar si es Doctor
        boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
        if (!exists) {
            sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
            return;
        }
        String errores = diagnosticoVal.listValid(params);
        if (!errores.isEmpty()) {
            sendResponse(fromEmail, "Error", errores);
            return;
        }
        try {
            if (params == null || params.isEmpty()) {
                sendResponse(fromEmail, "Error", "Use LISDIA[*]");
                return;
            }

            String parametro = params.get(0).trim();

            if (parametro.equals("*")) {
                sendResponse(fromEmail, "Listado de Diagnósticos", listAll().toString());
                return;
            }

            sendResponse(fromEmail, "Error", "Listado no permitido para Diagnósticos.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar diagnósticos: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        // Verificar si es Doctor
        boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
        if (!exists) {
            sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
            return;
        }
        String errores = diagnosticoVal.updateValid(params);
        if (!errores.isEmpty()) {
            sendResponse(fromEmail, "Error", errores);
            return;
        }
        try {
            // id[0], sintomas[1], tipoDiagnostico[2], gravedad[3], observaciones[4],
            // citaId[5]

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

            sendResponse(fromEmail, "Éxito",
                    "Diagnóstico con ID: " + diagnostico.getId() + " actualizado correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del diagnóstico y el ID de la cita deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar diagnóstico: " + e.getMessage());
        }
    }

    private void delete(List<String> params, String fromEmail) {
        // Verificar si es Doctor
        boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
        if (!exists) {
            sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
            return;
        }
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

        List<Diagnostico> diagnosticos = diagnosticoRepository.listarTodoConDetalles();

        StringBuilder sb = new StringBuilder();
        sb.append("Listado de Diagnósticos\n\n");

        if (diagnosticos.isEmpty()) {
            sb.append("No existen diagnósticos registrados.\n");
            return sb;
        }

        for (Diagnostico diagnostico : diagnosticos) {
            sb.append("--------------------------------------------------\n");
            sb.append("ID Diagnóstico: ").append(diagnostico.getId()).append("\n");
            sb.append("Síntomas: ").append(diagnostico.getSintomas()).append("\n");
            sb.append("Tipo diagnóstico: ").append(diagnostico.getTipoDiagnostico()).append("\n");
            sb.append("Gravedad: ").append(diagnostico.getGravedad()).append("\n");
            sb.append("Observaciones: ")
                    .append(diagnostico.getObservaciones() != null ? diagnostico.getObservaciones() : "N/A")
                    .append("\n");
            sb.append("ID Cita: ")
                    .append(diagnostico.getCita() != null ? diagnostico.getCita().getIdCita() : "N/A")
                    .append("\n");

            sb.append("Detalles:\n");

            if (diagnostico.getDetallesDiagnostico() == null || diagnostico.getDetallesDiagnostico().isEmpty()) {
                sb.append("  Sin detalles registrados.\n");
            } else {
                int i = 1;
                for (DetalleDiagnostico detalle : diagnostico.getDetallesDiagnostico()) {
                    sb.append("  ").append(i).append(") ")
                            .append("Zona bucal: ").append(detalle.getZonaBucal()).append("\n");
                    sb.append("     Observación: ")
                            .append(detalle.getObservacion() != null ? detalle.getObservacion() : "N/A")
                            .append("\n");

                    if (detalle.getDiente() != null) {
                        sb.append("     Diente ID: ").append(detalle.getDiente().getId()).append("\n");
                        sb.append("     Diente: ").append(detalle.getDiente().getNombre()).append("\n");
                    }

                    i++;
                }
            }

            sb.append("\n");
        }

        return sb;
    }

    private void cargarDetallesDesdeTexto(Diagnostico diagnostico, String detallesRaw) {
        String[] detalles = detallesRaw.split("\\|");

        for (int i = 0; i < detalles.length; i++) {
            String detalleTexto = detalles[i].trim();

            if (detalleTexto.isEmpty()) {
                continue;
            }

            String[] partes = detalleTexto.split(";", -1);

            if (partes.length < 3) {
                throw new IllegalArgumentException(
                        "El detalle N° " + (i + 1) + " está incompleto.\n"
                                + "Formato correcto del detalle:\n"
                                + "ObservacionDetalle;ZonaBucal;ID Diente");
            }

            String observacionDetalle = partes[0].trim();
            String zonaBucal = partes[1].trim();
            String dienteIdTexto = partes[2].trim();

            if (zonaBucal.isEmpty()) {
                throw new IllegalArgumentException("La zona bucal del detalle N° " + (i + 1) + " es obligatoria.");
            }

            if (dienteIdTexto.isEmpty()) {
                throw new IllegalArgumentException("El ID del diente del detalle N° " + (i + 1) + " es obligatorio.");
            }

            Long dienteId = Long.parseLong(dienteIdTexto);

            Diente diente = dienteRepository.findById(dienteId)
                    .orElseThrow(() -> new RuntimeException("Diente no encontrado con ID: " + dienteId));

            DetalleDiagnostico detalle = new DetalleDiagnostico();
            detalle.setObservacion(observacionDetalle);
            detalle.setZonaBucal(zonaBucal);
            detalle.setDiente(diente);

            diagnostico.addDetalleDiagnostico(detalle);
        }
    }

}
