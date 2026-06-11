package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.HistorialClinico;
import smail.sistema_mail_OdontoCool.entities.Tratamiento;
import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Service
public class TratamientoService {

    @Autowired
    private TratamientoRepository tratamientoRepository;
    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;
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

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            if (params.size() < 7) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un tratamiento.");
                return;
            }
            Tratamiento tratamiento = new Tratamiento();
            tratamiento.setObjetivoTratamiento(params.get(0));
            tratamiento.setObservacion(params.get(1));
            tratamiento.setEstado(params.get(2));
            tratamiento.setFechaInicio(LocalDate.parse(params.get(3)));
            tratamiento.setFechaFin(LocalDate.parse(params.get(4)));
            tratamiento.setFechaFinReal(LocalDate.parse(params.get(5)));

            //obtener el historial clinico del paciente 
            String historialId = params.get(6);
            HistorialClinico historialClinico = historialClinicoRepository.findById(historialId).orElse(null);
            tratamiento.setHistorialClinico(historialClinico);
            tratamientoRepository.save(tratamiento);
            sendResponse(fromEmail, "Éxito", "Tratamiento registrado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un tratamiento: " + e.getMessage());
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
            sendResponse(fromEmail, "Listado de Tratamientos", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar tratamientos: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            //Parametros: id[0], objetivo_tratamiento[1], observacion[2], estado[3], fecha_inicio[4], fecha_fin[5], fecha_fin_real[6]
            // if (params.size() < 7) {
            //     sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un tratamiento.");
            //     return;
            // }
            Long id = Long.parseLong(params.get(0));
            String objetivoTratamiento = params.get(1);
            String observacion = params.get(2);
            String estado = params.get(3);
            LocalDate fechaInicio = null;
            LocalDate fechaFin = null;
            LocalDate fechaFinReal = null;
            if (!params.get(4).trim().isEmpty()) {
                fechaInicio = LocalDate.parse(params.get(4));
            }
            if (!params.get(5).trim().isEmpty()) {
                fechaFin = LocalDate.parse(params.get(5));
            }
            if (!params.get(6).trim().isEmpty()) {
                fechaFinReal = LocalDate.parse(params.get(6));
            }
            Tratamiento tratamiento = tratamientoRepository.findById(id).orElse(null);
            if (tratamiento == null) {
                sendResponse(fromEmail, "Error", "Tratamiento con ID: " + id + " no encontrado.");
                return;
            }
            if (objetivoTratamiento != null && !objetivoTratamiento.trim().isEmpty()) {
                tratamiento.setObjetivoTratamiento(objetivoTratamiento);
            }
            if (observacion != null && !observacion.trim().isEmpty()) {
                tratamiento.setObservacion(observacion);
            }
            if (estado != null && !estado.trim().isEmpty()) {
                tratamiento.setEstado(estado);
            }
            if (fechaInicio != null) {
                tratamiento.setFechaInicio(fechaInicio);
            }
            if (fechaFin != null) {
                tratamiento.setFechaFin(fechaFin);
            }
            if (fechaFinReal != null) {
                tratamiento.setFechaFinReal(fechaFinReal);
            }
            tratamientoRepository.save(tratamiento);
            sendResponse(fromEmail, "Éxito", "Tratamiento: " + tratamiento.getId() + " actualizado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar tratamiento: " + e.getMessage());
        }
    }
    // TODO: implementar delete para tratamientos, se puede eliminar un tratamiento? o solo cambiar su estado a inactivo?

    private void delete(List<String> params, String fromEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private StringBuilder listAll() {
        List<Tratamiento> tratamientos = tratamientoRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Tratamientos\n");
        for (Tratamiento tratamiento : tratamientos) {
            sb.append(String.format("- [%s] Objetivo: %s Observación: %s Estado: %s\n Fecha Inicio: %s Fecha Fin: %s Fecha Fin Real: %s\n",
                    tratamiento.getId(),
                    tratamiento.getObjetivoTratamiento(),
                    tratamiento.getObservacion(),
                    tratamiento.getEstado(),
                    tratamiento.getFechaInicio(),
                    tratamiento.getFechaFin(),
                    tratamiento.getFechaFinReal()));
        }
        return sb;
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

}
