package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.ResultadoAnalisis;
import smail.sistema_mail_OdontoCool.entities.SolicitudAnalisis;
import smail.sistema_mail_OdontoCool.repositories.ResultadoAnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.SolicitudAnalisisRepository;

@Service
public class ResultadoAnalisisService {

    @Autowired
    private ResultadoAnalisisRepository resultadoAnalisisRepository;
    @Autowired
    private SolicitudAnalisisRepository solicitudAnalisisRepository;
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
                sendResponse(fromEmail, "Error", "Acción no permitida para ResultadoAnalisis.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // fechaResultado[0], resultado[1], observaciones[2], interpretacion[3],
            // archivoAdjunto[4], estado[5], solicitudAnalisisId[6]

            if (params.size() < 7) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un resultado de análisis.");
                return;
            }

            String fechaTexto = params.get(0).trim();
            String resultado = params.get(1).trim();
            String observaciones = params.get(2).trim();
            String interpretacion = params.get(3).trim();
            String archivoAdjunto = params.get(4).trim();
            String estado = params.get(5).trim();
            String solicitudIdTexto = params.get(6).trim();

            if (fechaTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "La fecha del resultado es obligatoria.");
                return;
            }

            if (resultado.isEmpty()) {
                sendResponse(fromEmail, "Error", "El resultado es obligatorio.");
                return;
            }

            if (estado.isEmpty()) {
                estado = "ACTIVO";
            }

            if (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("INACTIVO")) {
                sendResponse(fromEmail, "Error", "Estado inválido. Use ACTIVO o INACTIVO.");
                return;
            }

            if (solicitudIdTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID de la solicitud de análisis es obligatorio.");
                return;
            }

            Long solicitudId = Long.parseLong(solicitudIdTexto);

            SolicitudAnalisis solicitud = solicitudAnalisisRepository.findById(solicitudId)
                    .orElseThrow(
                            () -> new RuntimeException("Solicitud de análisis no encontrada con ID: " + solicitudId));

            ResultadoAnalisis resultadoAnalisis = new ResultadoAnalisis();
            resultadoAnalisis.setFechaResultado(LocalDate.parse(fechaTexto));
            resultadoAnalisis.setResultado(resultado);
            resultadoAnalisis.setObservaciones(observaciones);
            resultadoAnalisis.setInterpretacion(interpretacion);
            resultadoAnalisis.setArchivoAdjunto(archivoAdjunto);
            resultadoAnalisis.setEstado(estado.toUpperCase());
            resultadoAnalisis.setSolicitudAnalisis(solicitud);

            resultadoAnalisisRepository.save(resultadoAnalisis);

            sendResponse(fromEmail, "Éxito",
                    "Resultado de análisis registrado correctamente con ID: " + resultadoAnalisis.getId());

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID de la solicitud de análisis debe ser numérico.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar el resultado de análisis: " + e.getMessage());
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
            // id[0], fechaResultado[1], resultado[2], observaciones[3], interpretacion[4],
            // archivoAdjunto[5], estado[6], solicitudAnalisisId[7]

            if (params.size() < 8) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un resultado de análisis.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del resultado de análisis es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            ResultadoAnalisis resultadoAnalisis = resultadoAnalisisRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Resultado de análisis con ID: " + id + " no encontrado."));

            String fechaTexto = params.get(1).trim();
            String resultado = params.get(2).trim();
            String observaciones = params.get(3).trim();
            String interpretacion = params.get(4).trim();
            String archivoAdjunto = params.get(5).trim();
            String estado = params.get(6).trim();
            String solicitudIdTexto = params.get(7).trim();

            if (!fechaTexto.isEmpty()) {
                resultadoAnalisis.setFechaResultado(LocalDate.parse(fechaTexto));
            }

            if (!resultado.isEmpty()) {
                resultadoAnalisis.setResultado(resultado);
            }

            if (!observaciones.isEmpty()) {
                resultadoAnalisis.setObservaciones(observaciones);
            }

            if (!interpretacion.isEmpty()) {
                resultadoAnalisis.setInterpretacion(interpretacion);
            }

            if (!archivoAdjunto.isEmpty()) {
                resultadoAnalisis.setArchivoAdjunto(archivoAdjunto);
            }

            if (!estado.isEmpty()) {
                if (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("INACTIVO")) {
                    sendResponse(fromEmail, "Error", "Estado inválido. Use ACTIVO o INACTIVO.");
                    return;
                }

                resultadoAnalisis.setEstado(estado.toUpperCase());
            }

            if (!solicitudIdTexto.isEmpty()) {
                Long solicitudId = Long.parseLong(solicitudIdTexto);

                SolicitudAnalisis solicitud = solicitudAnalisisRepository.findById(solicitudId)
                        .orElseThrow(() -> new RuntimeException(
                                "Solicitud de análisis no encontrada con ID: " + solicitudId));

                resultadoAnalisis.setSolicitudAnalisis(solicitud);
            }

            resultadoAnalisisRepository.save(resultadoAnalisis);

            sendResponse(fromEmail, "Éxito",
                    "Resultado de análisis con ID: " + resultadoAnalisis.getId() + " actualizado correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del resultado y el ID de la solicitud deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar resultado de análisis: " + e.getMessage());
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
        List<ResultadoAnalisis> resultadoAnalisis = resultadoAnalisisRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Resultados de Análisis\n");
        for (ResultadoAnalisis a : resultadoAnalisis) {
            sb.append(String.format(
                    "- [%s] fechaResultado: %s Observaciones: %s Interpretación: %s, Archivo: %s, Estado: %s\n",
                    a.getId(),
                    a.getFechaResultado(),
                    a.getObservaciones(),
                    a.getInterpretacion(),
                    a.getArchivoAdjunto(),
                    a.getEstado()));
        }
        return sb;
    }

}
