package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Diente;
import smail.sistema_mail_OdontoCool.entities.Tratamiento;
import smail.sistema_mail_OdontoCool.entities.TratamientoDiente;
import smail.sistema_mail_OdontoCool.repositories.DienteRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoDienteRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Service
public class TratamientoDienteService {

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private TratamientoDienteRepository tratamientoDienteRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;
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

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: caraDental[0], observacion[1], fechaRegistro[2], estado[3],
            // tratamientoPlanificado[4], tratamientoId[5], dienteId[6]

            if (params.size() < 7) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para registrar un tratamiento de diente.");
                return;
            }

            String caraDental = params.get(0);
            String observacion = params.get(1);
            LocalDate fechaRegistro = LocalDate.parse(params.get(2));
            String estado = params.get(3);
            String tratamientoPlanificado = params.get(4);
            Long tratamientoId = Long.parseLong(params.get(5));
            Long dienteId = Long.parseLong(params.get(6));

            if (caraDental.trim().isEmpty()
                    || fechaRegistro == null
                    || estado.trim().isEmpty()
                    || tratamientoPlanificado.trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "Faltan parámetros obligatorios para registrar un tratamiento de diente.");
                return;
            }

            Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                    .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado con ID: " + tratamientoId));

            Diente diente = dienteRepository.findById(dienteId)
                    .orElseThrow(() -> new RuntimeException("Diente no encontrado con ID: " + dienteId));

            TratamientoDiente tratamientoDiente = new TratamientoDiente();
            tratamientoDiente.setCaraDental(caraDental);
            tratamientoDiente.setObservacion(observacion);
            tratamientoDiente.setFechaRegistro(fechaRegistro);
            tratamientoDiente.setEstado(estado);
            tratamientoDiente.setTratamientoPlanificado(tratamientoPlanificado);
            tratamientoDiente.setTratamiento(tratamiento);
            tratamientoDiente.setDiente(diente);

            tratamientoDienteRepository.save(tratamientoDiente);

            sendResponse(fromEmail, "Éxito",
                    "Tratamiento de diente registrado correctamente con ID: " + tratamientoDiente.getId());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un tratamiento de diente: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Tratamientos de Dientes.");
                }
            }
            sendResponse(fromEmail, "Listado de Tratamientos de Dientes", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar tratamientos de dientes: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // id[0], caraDental[1], observacion[2], fecha_registro[3], estado[4],
            // tratamientoPlanificado[5], tratamientoId[6], dienteId[7]

            if (params.size() < 8) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un tratamiento de diente.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del tratamiento de diente es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            TratamientoDiente tratamientoDiente = tratamientoDienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tratamiento de diente con ID: " + id + " no encontrado."));

            String caraDental = params.get(1).trim();
            String observacion = params.get(2).trim();
            String fechaTexto = params.get(3).trim();
            String estado = params.get(4).trim();
            String tratamientoPlanificado = params.get(5).trim();
            String tratamientoIdTexto = params.get(6).trim();
            String dienteIdTexto = params.get(7).trim();

            if (!caraDental.isEmpty()) {
                tratamientoDiente.setCaraDental(caraDental);
            }

            if (!observacion.isEmpty()) {
                tratamientoDiente.setObservacion(observacion);
            }

            if (!fechaTexto.isEmpty()) {
                tratamientoDiente.setFechaRegistro(LocalDate.parse(fechaTexto));
            }

            if (!estado.isEmpty()) {
                tratamientoDiente.setEstado(estado);
            }

            if (!tratamientoPlanificado.isEmpty()) {
                tratamientoDiente.setTratamientoPlanificado(tratamientoPlanificado);
            }

            if (!tratamientoIdTexto.isEmpty()) {
                Long tratamientoId = Long.parseLong(tratamientoIdTexto);

                Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                        .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado con ID: " + tratamientoId));

                tratamientoDiente.setTratamiento(tratamiento);
            }

            if (!dienteIdTexto.isEmpty()) {
                Long dienteId = Long.parseLong(dienteIdTexto);

                Diente diente = dienteRepository.findById(dienteId)
                        .orElseThrow(() -> new RuntimeException("Diente no encontrado con ID: " + dienteId));

                tratamientoDiente.setDiente(diente);
            }

            tratamientoDienteRepository.save(tratamientoDiente);

            sendResponse(fromEmail, "Éxito",
                    "Tratamiento de diente actualizado correctamente con ID: " + tratamientoDiente.getId());

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID, tratamientoId y dienteId deben ser numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar tratamiento de diente: " + e.getMessage());
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
        List<TratamientoDiente> tratamientos = tratamientoDienteRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Tratamientos de Dientes\n");
        for (TratamientoDiente tratamientoDiente : tratamientos) {
            sb.append(String.format("- [%s] Cara Dental: %s Observación: %s Fecha Registro: %s Estado: %s Tratamiento Planificado: %s\n",
                    tratamientoDiente.getId(),
                    tratamientoDiente.getCaraDental(),
                    tratamientoDiente.getObservacion(),
                    tratamientoDiente.getFechaRegistro(),
                    tratamientoDiente.getEstado(),
                    tratamientoDiente.getTratamientoPlanificado()));
        }
        return sb;
    }

}
