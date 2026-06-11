package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            // fechaEmision[0], observaciones[1], tratamientoId[2]

            if (params.size() < 3) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar una receta/recomendación.");
                return;
            }

            String fechaTexto = params.get(0).trim();
            String observaciones = params.get(1).trim();
            String tratamientoIdTexto = params.get(2).trim();

            if (fechaTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "La fecha de emisión es obligatoria.");
                return;
            }

            if (tratamientoIdTexto.isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del tratamiento es obligatorio.");
                return;
            }

            Long tratamientoId = Long.parseLong(tratamientoIdTexto);

            Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                    .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado con ID: " + tratamientoId));

            RecetaRecomendacion receta = new RecetaRecomendacion();
            receta.setFechaEmision(LocalDate.parse(fechaTexto));
            receta.setObservaciones(observaciones);
            receta.setTratamiento(tratamiento);

            recetaRecomendacionRepository.save(receta);

            sendResponse(fromEmail, "Éxito",
                    "Receta/Recomendación registrada correctamente con ID: " + receta.getId());

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del tratamiento debe ser numérico.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar la receta/recomendación: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Recetas.");
                }
            }
            sendResponse(fromEmail, "Listado de Recetas", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar recetas: " + e.getMessage());
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
        List<RecetaRecomendacion> recetaRecomendaciones = recetaRecomendacionRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Recetas\n");
        for (RecetaRecomendacion receta : recetaRecomendaciones) {
            sb.append(String.format("- [%s] fechaEmision: %s Observación: %s\n tratamiento: %s Observacion: %s\n",
                    receta.getId(),
                    receta.getFechaEmision(),
                    receta.getObservaciones(),
                    receta.getTratamiento() != null ? receta.getTratamiento().getObjetivoTratamiento() : "N/A",
                    receta.getTratamiento() != null ? receta.getTratamiento().getObservacion() : "N/A"
            ));
        }
        return sb;

    }
}
