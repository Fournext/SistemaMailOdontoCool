package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smail.sistema_mail_OdontoCool.entities.DetalleRecomendacion;
import smail.sistema_mail_OdontoCool.entities.RecetaRecomendacion;
import smail.sistema_mail_OdontoCool.entities.Tratamiento;
import smail.sistema_mail_OdontoCool.repositories.RecetaRecomendacionRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Service
public class RecetaRecomendacionService {

    @Autowired
    private RecetaRecomendacionRepository recetaRecomendacionRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private TratamientoRepository tratamientoRepository;

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
            /*
             * Formato:
             * INSREC["FechaEmision", "Observaciones", "ID Tratamiento", "Descripcion;Dosis;Duracion;Frecuencia | Descripcion;Dosis;Duracion;Frecuencia"]
             */

            if (params.size() < 4) {
                sendResponse(fromEmail, "Error",
                        "Parámetros insuficientes.\n"
                        + "Formato correcto:\n"
                        + "INSREC[\"FechaEmision\", \"Observaciones\", \"ID Tratamiento\", \"Descripcion;Dosis;Duracion;Frecuencia | Descripcion;Dosis;Duracion;Frecuencia\"]");
                return;
            }

            String fechaTexto = params.get(0).trim();
            String observaciones = params.get(1).trim();
            String tratamientoIdTexto = params.get(2).trim();
            String detallesRaw = params.get(3).trim();

            if (fechaTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "La fecha de emisión es obligatoria.");
                return;
            }

            if (tratamientoIdTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del tratamiento es obligatorio.");
                return;
            }

            if (detallesRaw.isEmpty()) {
                sendResponse(fromEmail, "Error", "Debe enviar al menos un detalle de recomendación.");
                return;
            }

            Long tratamientoId = Long.parseLong(tratamientoIdTexto);

            Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                    .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado con ID: " + tratamientoId));

            RecetaRecomendacion receta = new RecetaRecomendacion();
            receta.setFechaEmision(LocalDate.parse(fechaTexto));
            receta.setObservaciones(observaciones);
            receta.setTratamiento(tratamiento);

            cargarDetallesDesdeTexto(receta, detallesRaw);

            if (receta.getDetallesRecomendacion().isEmpty()) {
                sendResponse(fromEmail, "Error", "Debe registrar al menos un detalle válido.");
                return;
            }

            recetaRecomendacionRepository.save(receta);

            sendResponse(fromEmail, "Éxito",
                    "Receta/Recomendación registrada correctamente.\n"
                    + "ID Receta/Recomendación: " + receta.getId() + "\n"
                    + "Detalles registrados: " + receta.getDetallesRecomendacion().size());

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del tratamiento debe ser numérico.");
        } catch (DateTimeParseException e) {
            sendResponse(fromEmail, "Error", "La fecha debe tener el formato AAAA-MM-DD. Ejemplo: 2026-06-15");
        } catch (IllegalArgumentException e) {
            sendResponse(fromEmail, "Error", e.getMessage());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar la receta/recomendación: " + e.getMessage());
        }
    }

    private void cargarDetallesDesdeTexto(RecetaRecomendacion receta, String detallesRaw) {
        String[] detalles = detallesRaw.split("\\|");

        for (int i = 0; i < detalles.length; i++) {
            String detalleTexto = detalles[i].trim();

            if (detalleTexto.isEmpty()) {
                continue;
            }

            String[] partes = detalleTexto.split(";", -1);

            if (partes.length < 4) {
                throw new IllegalArgumentException(
                        "El detalle N° " + (i + 1) + " está incompleto.\n"
                        + "Formato correcto del detalle:\n"
                        + "Descripcion;Dosis;Duracion;Frecuencia");
            }

            String descripcion = partes[0].trim();
            String dosis = partes[1].trim();
            String duracion = partes[2].trim();
            String frecuencia = partes[3].trim();

            if (descripcion.isEmpty()) {
                throw new IllegalArgumentException("La descripción del detalle N° " + (i + 1) + " es obligatoria.");
            }

            DetalleRecomendacion detalle = new DetalleRecomendacion();
            detalle.setDescripcion(descripcion);
            detalle.setDosis(dosis);
            detalle.setDuracion(duracion);
            detalle.setFrecuencia(frecuencia);

            receta.addDetalleRecomendacion(detalle);
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            if (params == null || params.isEmpty()) {
                sendResponse(fromEmail, "Error", "Use LISREC[*] o LISREC[\"Tratamiento:ID\"]");
                return;
            }

            String parametro = params.get(0).trim();

            if (parametro.equals("*")) {
                sendResponse(fromEmail, "Listado de Recetas/Recomendaciones", listAll().toString());
                return;
            }

            if (parametro.startsWith("Tratamiento:")) {
                String idTexto = parametro.substring("Tratamiento:".length()).trim();

                if (idTexto.isEmpty()) {
                    sendResponse(fromEmail, "Error", "Debe indicar el ID del tratamiento.");
                    return;
                }

                sendResponse(fromEmail, "Listado de Recetas/Recomendaciones", listByTratamiento(idTexto).toString());
                return;
            }

            sendResponse(fromEmail, "Error", "Listado no permitido. Use LISREC[*] o LISREC[\"Tratamiento:ID\"]");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar recetas/recomendaciones: " + e.getMessage());
        }
    }

    private StringBuilder listByTratamiento(String tratamientoIdTexto) {
        try {
            Long tratamientoId = Long.parseLong(tratamientoIdTexto);

            List<RecetaRecomendacion> recetas
                    = recetaRecomendacionRepository.listarPorTratamientoConDetalles(tratamientoId);

            return construirListado(
                    recetas,
                    "Listado de Recetas/Recomendaciones del Tratamiento ID: " + tratamientoId
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID del tratamiento debe ser numérico.");
        }
    }

    private void update(List<String> params, String fromEmail) {
        try {
            // id[0], fechaEmision[1], observaciones[2], tratamientoId[3]

            if (params.size() < 4) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar una receta/recomendación.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID de la receta/recomendación es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            RecetaRecomendacion receta = recetaRecomendacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Receta/Recomendación con ID: " + id + " no encontrada."));

            String fechaTexto = params.get(1).trim();
            String observaciones = params.get(2).trim();
            String tratamientoIdTexto = params.get(3).trim();

            if (!fechaTexto.isEmpty()) {
                receta.setFechaEmision(LocalDate.parse(fechaTexto));
            }

            if (!observaciones.isEmpty()) {
                receta.setObservaciones(observaciones);
            }

            if (!tratamientoIdTexto.isEmpty()) {
                Long tratamientoId = Long.parseLong(tratamientoIdTexto);

                Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                        .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado con ID: " + tratamientoId));

                receta.setTratamiento(tratamiento);
            }

            recetaRecomendacionRepository.save(receta);

            sendResponse(fromEmail, "Éxito",
                    "Receta/Recomendación con ID: " + receta.getId() + " actualizada correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID de la receta y el ID del tratamiento deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar receta/recomendación: " + e.getMessage());
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
        List<RecetaRecomendacion> recetas = recetaRecomendacionRepository.listarTodoConDetalles();
        return construirListado(recetas, "Listado de todas las Recetas/Recomendaciones");
    }

    private StringBuilder construirListado(List<RecetaRecomendacion> recetas, String titulo) {
        StringBuilder sb = new StringBuilder();
        sb.append(titulo).append("\n\n");

        if (recetas == null || recetas.isEmpty()) {
            sb.append("No existen recetas/recomendaciones registradas.\n");
            return sb;
        }

        for (RecetaRecomendacion receta : recetas) {
            sb.append("--------------------------------------------------\n");
            sb.append("ID Receta/Recomendación: ").append(receta.getId()).append("\n");
            sb.append("Fecha de emisión: ").append(receta.getFechaEmision()).append("\n");
            sb.append("Observaciones: ")
                    .append(receta.getObservaciones() != null ? receta.getObservaciones() : "N/A")
                    .append("\n");

            sb.append("Recomendaciones:\n");

            if (receta.getDetallesRecomendacion() == null || receta.getDetallesRecomendacion().isEmpty()) {
                sb.append("  Sin recomendaciones registradas.\n");
            } else {
                int i = 1;
                for (DetalleRecomendacion detalle : receta.getDetallesRecomendacion()) {
                    sb.append("  ").append(i).append(") ")
                            .append(detalle.getDescripcion() != null ? detalle.getDescripcion() : "N/A")
                            .append("\n");
                    sb.append("     Dosis: ")
                            .append(detalle.getDosis() != null ? detalle.getDosis() : "N/A")
                            .append("\n");
                    sb.append("     Duración: ")
                            .append(detalle.getDuracion() != null ? detalle.getDuracion() : "N/A")
                            .append("\n");
                    sb.append("     Frecuencia: ")
                            .append(detalle.getFrecuencia() != null ? detalle.getFrecuencia() : "N/A")
                            .append("\n");
                    i++;
                }
            }

            sb.append("\n");
        }

        return sb;
    }
}
