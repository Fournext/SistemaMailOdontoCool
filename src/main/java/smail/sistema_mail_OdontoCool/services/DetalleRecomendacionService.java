package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smail.sistema_mail_OdontoCool.entities.DetalleRecomendacion;
import smail.sistema_mail_OdontoCool.entities.RecetaRecomendacion;
import smail.sistema_mail_OdontoCool.repositories.DetalleRecomendacionRepository;
import smail.sistema_mail_OdontoCool.repositories.RecetaRecomendacionRepository;

@Service
public class DetalleRecomendacionService {

    @Autowired
    private RecetaRecomendacionRepository recetaRecomendacionRepository;

    @Autowired
    private DetalleRecomendacionRepository detalleRecomendacionRepository;

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

    private void insert(List<String> params, String fromEmail) {
        try {
            // descripcion[0], dosis[1], duracion[2], frecuencia[3],
            // recetaRecomendacionId[4]

            if (params.size() < 5) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un detalle de recomendación.");
                return;
            }

            String descripcion = params.get(0).trim();
            String dosis = params.get(1).trim();
            String duracion = params.get(2).trim();
            String frecuencia = params.get(3).trim();
            String recetaIdTexto = params.get(4).trim();

            if (descripcion.isEmpty()) {
                sendResponse(fromEmail, "Error", "La descripción es obligatoria.");
                return;
            }

            if (recetaIdTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID de la receta/recomendación es obligatorio.");
                return;
            }

            Long recetaId = Long.parseLong(recetaIdTexto);

            RecetaRecomendacion receta = recetaRecomendacionRepository.findById(recetaId)
                    .orElseThrow(() -> new RuntimeException("Receta/Recomendación no encontrada con ID: " + recetaId));

            DetalleRecomendacion detalle = new DetalleRecomendacion();
            detalle.setDescripcion(descripcion);
            detalle.setDosis(dosis);
            detalle.setDuracion(duracion);
            detalle.setFrecuencia(frecuencia);
            detalle.setRecetaRecomendacion(receta);

            detalleRecomendacionRepository.save(detalle);

            sendResponse(fromEmail, "Éxito",
                    "Detalle de recomendación registrado correctamente con ID: " + detalle.getId());

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID de la receta/recomendación debe ser numérico.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar el detalle de recomendación: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Tratamientos.");
                }
            }
            sendResponse(fromEmail, "Listado de Tratamientos", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar tratamientos: " + e.getMessage());
        }
    }

    private void update(List<String> params, String fromEmail) {
        try {
            // id[0], descripcion[1], dosis[2], duracion[3], frecuencia[4],
            // recetaRecomendacionId[5]

            if (params.size() < 6) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un detalle de recomendación.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del detalle de recomendación es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            DetalleRecomendacion detalle = detalleRecomendacionRepository.findById(id)
                    .orElseThrow(
                            () -> new RuntimeException("Detalle de recomendación con ID: " + id + " no encontrado."));

            String descripcion = params.get(1).trim();
            String dosis = params.get(2).trim();
            String duracion = params.get(3).trim();
            String frecuencia = params.get(4).trim();
            String recetaIdTexto = params.get(5).trim();

            if (!descripcion.isEmpty()) {
                detalle.setDescripcion(descripcion);
            }

            if (!dosis.isEmpty()) {
                detalle.setDosis(dosis);
            }

            if (!duracion.isEmpty()) {
                detalle.setDuracion(duracion);
            }

            if (!frecuencia.isEmpty()) {
                detalle.setFrecuencia(frecuencia);
            }

            if (!recetaIdTexto.isEmpty()) {
                Long recetaId = Long.parseLong(recetaIdTexto);

                RecetaRecomendacion receta = recetaRecomendacionRepository.findById(recetaId)
                        .orElseThrow(
                                () -> new RuntimeException("Receta/Recomendación no encontrada con ID: " + recetaId));

                detalle.setRecetaRecomendacion(receta);
            }

            detalleRecomendacionRepository.save(detalle);

            sendResponse(fromEmail, "Éxito",
                    "Detalle de recomendación con ID: " + detalle.getId() + " actualizado correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error",
                    "El ID del detalle y el ID de la receta/recomendación deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar detalle de recomendación: " + e.getMessage());
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
        List<DetalleRecomendacion> tratamientos = detalleRecomendacionRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Detalles de Recomendaciones\n");
        for (DetalleRecomendacion detalle : tratamientos) {
            sb.append(String.format(
                    "- [%s] descripcion: %s dosis: %s duracion: %s frecuencia: %s\n RecetaRecomendacionID: %s\n",
                    detalle.getId(),
                    detalle.getDescripcion(),
                    detalle.getDosis(),
                    detalle.getDuracion(),
                    detalle.getFrecuencia(),
                    detalle.getRecetaRecomendacion().getId()));
        }
        return sb;
    }

}
