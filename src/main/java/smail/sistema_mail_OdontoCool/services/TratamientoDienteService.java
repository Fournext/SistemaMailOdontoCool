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
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.validations.TratamientoDienteVal;

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

    @Autowired
    private TratamientoDienteVal tratamientoDienteVal;

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
                sendResponse(fromEmail, "Error", "Acción no permitida para Tratamientos de Dientes.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = tratamientoDienteVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String detallesRaw = params.get(0).trim();
            String[] registros = detallesRaw.split("\\s*\\|\\s*");

            int cantidadInsertada = 0;

            for (String registro : registros) {
                String[] campos = registro.split("\\s*;\\s*", -1);

                String caraDental = campos[0].trim();
                String observacion = campos[1].trim();
                LocalDate fechaRegistro = LocalDate.parse(campos[2].trim().replace('/', '-'));
                String estado = campos[3].trim().toUpperCase();
                String tratamientoPlanificado = campos[4].trim();
                Long tratamientoId = Long.parseLong(campos[5].trim());
                Long dienteId = Long.parseLong(campos[6].trim());

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
                cantidadInsertada++;
            }

            sendResponse(fromEmail, "Éxito",
                    "Se registraron correctamente " + cantidadInsertada + " tratamientos de dientes.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "No se pudieron registrar los tratamientos de dientes: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            String validationMsg = tratamientoDienteVal.listValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            StringBuilder sb;
            String parametro = params.get(0).trim();

            if ("*".equals(parametro)) {
                // Verificar si es Doctor
                boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
                if (!exists) {
                    sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                    return;
                }
                sb = listAll();
            } else {
                // Verificar si es Doctor o paciente
                boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR")
                        || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PACIENTE");
                if (!exists) {
                    sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                    return;
                }
                String tratamientoIdTexto = parametro.substring("Tratamiento:".length()).trim();
                Long tratamientoId = Long.parseLong(tratamientoIdTexto);
                sb = findByTratamiento(tratamientoId);
            }

            sendResponse(fromEmail, "Listado de Tratamientos de Dientes", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar tratamientos de dientes: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = tratamientoDienteVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
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
                tratamientoDiente.setFechaRegistro(LocalDate.parse(fechaTexto.replace('/', '-')));
            }

            if (!estado.isEmpty()) {
                tratamientoDiente.setEstado(estado.toUpperCase());
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

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "Error al actualizar tratamiento de diente: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params == null || params.size() != 1 || params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "Debe enviar el ID del tratamiento de diente.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            TratamientoDiente tratamientoDiente = tratamientoDienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tratamiento de diente con ID: " + id + " no encontrado."));

            tratamientoDiente.setEstado("INACTIVO");
            tratamientoDienteRepository.save(tratamientoDiente);

            sendResponse(fromEmail, "Éxito",
                    "Tratamiento de diente desactivado correctamente con ID: " + id);

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID debe ser numérico.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar tratamiento de diente: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<TratamientoDiente> tratamientos = tratamientoDienteRepository.findAll();
        StringBuilder sb = new StringBuilder("Listado de Tratamientos de Dientes\n");

        for (TratamientoDiente tratamientoDiente : tratamientos) {
            sb.append(formatearTratamientoDiente(tratamientoDiente));
        }

        return sb;
    }

    private StringBuilder findByTratamiento(Long tratamientoId) {
        List<TratamientoDiente> tratamientos = tratamientoDienteRepository.findByTratamientoId(tratamientoId);
        StringBuilder sb = new StringBuilder();

        sb.append("Listado de Tratamientos de Dientes para Tratamiento ID: ")
                .append(tratamientoId)
                .append("\n");

        for (TratamientoDiente tratamientoDiente : tratamientos) {
            sb.append(formatearTratamientoDiente(tratamientoDiente));
        }

        if (tratamientos.isEmpty()) {
            sb.append("No se encontraron tratamientos de dientes para ese tratamiento.\n");
        }

        return sb;
    }

    private String formatearTratamientoDiente(TratamientoDiente tratamientoDiente) {
        return String.format(
                "- [%s] Cara Dental: %s | Observación: %s | Fecha Registro: %s | Estado: %s | Tratamiento Planificado: %s | Tratamiento ID: %s | Diente ID: %s\n",
                tratamientoDiente.getId(),
                tratamientoDiente.getCaraDental(),
                tratamientoDiente.getObservacion(),
                tratamientoDiente.getFechaRegistro(),
                tratamientoDiente.getEstado(),
                tratamientoDiente.getTratamientoPlanificado(),
                tratamientoDiente.getTratamiento() != null ? tratamientoDiente.getTratamiento().getId() : "N/A",
                tratamientoDiente.getDiente() != null ? tratamientoDiente.getDiente().getId() : "N/A");
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }
}