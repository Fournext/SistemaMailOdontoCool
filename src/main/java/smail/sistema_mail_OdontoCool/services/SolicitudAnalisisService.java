package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Analisis;
import smail.sistema_mail_OdontoCool.entities.ResultadoAnalisis;
import smail.sistema_mail_OdontoCool.entities.SolicitudAnalisis;
import smail.sistema_mail_OdontoCool.entities.Tratamiento;
import smail.sistema_mail_OdontoCool.repositories.AnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.ResultadoAnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.SolicitudAnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;
import smail.sistema_mail_OdontoCool.validations.SolicitudAnalisisVal;

@Service
public class SolicitudAnalisisService {

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private SolicitudAnalisisRepository solicitudAnalisisRepository;

    @Autowired
    private AnalisisRepository analisisRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private ResultadoAnalisisRepository resultadoAnalisisRepository;

    @Autowired
    private SolicitudAnalisisVal solicitudAnalisisVal;

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
            case "AGR":
                agregarResultado(params, fromEmail);
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
            String validationMsg = solicitudAnalisisVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String fechaTexto = params.get(0).trim();
            String motivo = params.get(1).trim();
            String estado = params.get(2).trim();
            String analisisIdTexto = params.get(3).trim();
            String tratamientoIdTexto = params.size() > 4 ? params.get(4).trim() : "";

            if (estado.isEmpty()) {
                estado = "ACTIVO";
            }

            Long analisisId = Long.parseLong(analisisIdTexto);

            Analisis analisis = analisisRepository.findById(analisisId)
                    .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + analisisId));

            Tratamiento tratamiento = null;

            if (!tratamientoIdTexto.isEmpty()) {
                Long tratamientoId = Long.parseLong(tratamientoIdTexto);
                tratamiento = tratamientoRepository.findById(tratamientoId)
                        .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado con ID: " + tratamientoId));
            }

            SolicitudAnalisis solicitud = new SolicitudAnalisis();
            solicitud.setFechaSolicitud(LocalDate.parse(fechaTexto.replace('/', '-')));
            solicitud.setMotivo(motivo);
            solicitud.setEstado(estado.toUpperCase());
            solicitud.setAnalisis(analisis);
            solicitud.setTratamiento(tratamiento);

            solicitudAnalisisRepository.save(solicitud);

            sendResponse(fromEmail, "Éxito",
                    "Solicitud de análisis registrada correctamente con ID: " + solicitud.getId());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "No se pudo registrar la solicitud de análisis: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            String validationMsg = solicitudAnalisisVal.listValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String parametro = params.get(0).trim();
            StringBuilder sb;

            if ("*".equals(parametro)) {
                sb = listAll();
            } else {
                Long tratamientoId = Long.parseLong(
                        parametro.substring("Tratamiento:".length()).trim()
                );
                sb = findByTratamientoId(tratamientoId);
            }

            sendResponse(fromEmail, "Listado de Solicitudes de Análisis", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar análisis: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            String validationMsg = solicitudAnalisisVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            SolicitudAnalisis solicitud = solicitudAnalisisRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud de análisis con ID: " + id + " no encontrada."));

            String fechaTexto = params.get(1).trim();
            String motivo = params.get(2).trim();
            String estado = params.get(3).trim();
            String analisisIdTexto = params.get(4).trim();
            String tratamientoIdTexto = params.get(5).trim();

            if (!fechaTexto.isEmpty()) {
                solicitud.setFechaSolicitud(LocalDate.parse(fechaTexto.replace('/', '-')));
            }

            if (!motivo.isEmpty()) {
                solicitud.setMotivo(motivo);
            }

            if (!estado.isEmpty()) {
                solicitud.setEstado(estado.toUpperCase());
            }

            if (!analisisIdTexto.isEmpty()) {
                Long analisisId = Long.parseLong(analisisIdTexto);
                Analisis analisis = analisisRepository.findById(analisisId)
                        .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + analisisId));
                solicitud.setAnalisis(analisis);
            }

            if (!tratamientoIdTexto.isEmpty()) {
                Long tratamientoId = Long.parseLong(tratamientoIdTexto);
                Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                        .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado con ID: " + tratamientoId));
                solicitud.setTratamiento(tratamiento);
            }

            solicitudAnalisisRepository.save(solicitud);

            sendResponse(fromEmail, "Éxito",
                    "Solicitud de análisis con ID: " + solicitud.getId() + " actualizada correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "Error al actualizar solicitud de análisis: " + e.getMessage());
        }
    }

    @Transactional
    private void agregarResultado(List<String> params, String fromEmail) {
        try {
            String validationMsg = solicitudAnalisisVal.agregarResultadoValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long solicitudId = Long.parseLong(params.get(0).trim());
            LocalDate fechaResultado = LocalDate.parse(params.get(1).trim().replace('/', '-'));
            String resultado = params.get(2).trim();
            String observaciones = params.get(3).trim();
            String interpretacion = params.get(4).trim();
            String estado = params.get(5).trim().toUpperCase();
            String archivoAdjunto = params.get(6).trim();

            SolicitudAnalisis solicitud = solicitudAnalisisRepository.findById(solicitudId)
                    .orElseThrow(() -> new RuntimeException("Solicitud de análisis con ID: " + solicitudId + " no encontrada."));

            ResultadoAnalisis resultadoAnalisis = new ResultadoAnalisis();
            resultadoAnalisis.setFechaResultado(fechaResultado);
            resultadoAnalisis.setResultado(resultado);
            resultadoAnalisis.setObservaciones(observaciones);
            resultadoAnalisis.setInterpretacion(interpretacion);
            resultadoAnalisis.setEstado(estado);
            resultadoAnalisis.setArchivoAdjunto(archivoAdjunto);
            resultadoAnalisis.setSolicitudAnalisis(solicitud);

            resultadoAnalisisRepository.save(resultadoAnalisis);

            solicitud.setResultadoAnalisis(resultadoAnalisis);
            solicitud.setEstado("FINALIZADO");
            solicitudAnalisisRepository.save(solicitud);

            sendResponse(fromEmail, "Éxito",
                    "Resultado de análisis agregado correctamente a la solicitud con ID: " + solicitudId);

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "Error al agregar resultado de análisis: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            String validationMsg = solicitudAnalisisVal.deleteValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            SolicitudAnalisis solicitud = solicitudAnalisisRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud de análisis con ID: " + id + " no encontrada."));

            solicitud.setEstado("INACTIVO");
            solicitudAnalisisRepository.save(solicitud);

            sendResponse(fromEmail, "Éxito",
                    "Solicitud de análisis con ID: " + id + " desactivada correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "Error al eliminar solicitud de análisis: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<SolicitudAnalisis> solicitudes = solicitudAnalisisRepository.findAll();

        StringBuilder sb = new StringBuilder("Listado de Solicitudes de Análisis\n");

        for (SolicitudAnalisis s : solicitudes) {
            sb.append(formatearSolicitud(s));
        }

        return sb;
    }

    private StringBuilder findByTratamientoId(Long tratamientoId) {
        List<SolicitudAnalisis> solicitudes = solicitudAnalisisRepository.findByTratamientoId(tratamientoId);

        StringBuilder sb = new StringBuilder();
        sb.append("Solicitudes de Análisis para Tratamiento ID: ")
                .append(tratamientoId)
                .append("\n");

        if (solicitudes.isEmpty()) {
            sb.append("No se encontraron solicitudes de análisis para este tratamiento.\n");
            return sb;
        }

        for (SolicitudAnalisis s : solicitudes) {
            ResultadoAnalisis resultado = resultadoAnalisisRepository.findBySolicitudAnalisisId(s.getId());

            if (resultado != null) {
                s.setResultadoAnalisis(resultado);
            }

            sb.append(formatearSolicitud(s));
        }

        return sb;
    }

    private String formatearSolicitud(SolicitudAnalisis s) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "- [%s] Fecha: %s | Motivo: %s | Estado: %s | Análisis: %s | Tratamiento: %s\n",
                s.getId(),
                s.getFechaSolicitud(),
                s.getMotivo(),
                s.getEstado(),
                s.getAnalisis() != null ? s.getAnalisis().getNombre() : "N/A",
                s.getTratamiento() != null ? s.getTratamiento().getId() : "N/A"
        ));

        if (s.getResultadoAnalisis() != null) {
            ResultadoAnalisis r = s.getResultadoAnalisis();

            sb.append(String.format(
                    "  Resultado ID: %s | Fecha: %s | Resultado: %s | Observaciones: %s | Interpretación: %s | Estado: %s | Archivo: %s\n",
                    r.getId(),
                    r.getFechaResultado(),
                    r.getResultado(),
                    r.getObservaciones(),
                    r.getInterpretacion(),
                    r.getEstado(),
                    r.getArchivoAdjunto()
            ));
        }

        return sb.toString();
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }
}